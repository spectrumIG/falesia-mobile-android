package it.softsamurai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import it.softsamurai.domain.model.GeoPoint
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import java.util.Locale

const val TAG_CRAG_MAP = "crag_map"

// OpenFreeMap: OpenStreetMap vector tiles, free and without API key (doc/tech-stack.md).
private const val STYLE_LIGHT = "https://tiles.openfreemap.org/styles/liberty"
private const val STYLE_DARK = "https://tiles.openfreemap.org/styles/dark"

/** A font both OpenFreeMap styles ship glyphs for. */
private const val MAP_FONT = "Noto Sans Regular"
private const val PARKING_LETTER = "P"
private const val HALO_ALPHA = 0.2f

private const val INITIAL_ZOOM = 15.0
private const val MIN_ZOOM = 3.0
private const val MAX_ZOOM = 19.0
private val MapHeight = 220.dp
private val ControlSize = 40.dp
private val ControlIconSize = 20.dp
private val SectorDotSize = 10.dp
private val SectorRadius = 5.dp
private val ParkingRadius = 10.dp
private val CragRadius = 9.dp
private val CragHaloRadius = 22.dp
private val PinStroke = 2.dp
private val FitPadding = 48.dp

/**
 * The crag on an OpenStreetMap map (design: Location section of screens/CragDetails.dc.html): the crag
 * pin, its sectors and the parking, with the crag name, zoom and recenter controls and the parking chip
 * drawn over it. The camera starts fitted to all the points; recenter goes back there.
 */
@Composable
fun CragMap(
    nome: String,
    crag: GeoPoint,
    modifier: Modifier = Modifier,
    parking: GeoPoint? = null,
    settori: List<GeoPoint> = emptyList(),
    parkingLabel: String? = null,
) {
    val colors = FalesiaTheme.colors
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    val mapDescription = stringResource(R.string.cd_map, nome)
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = crag.toPosition(), zoom = INITIAL_ZOOM),
    )
    val points = remember(crag, parking, settori) { listOfNotNull(crag, parking) + settori }
    val scope = rememberCoroutineScope()

    val inspectionMode = LocalInspectionMode.current
    LaunchedEffect(points) { if (!inspectionMode) cameraState.fitTo(points) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MapHeight)
                .clip(shape)
                .border(FalesiaTheme.size.hairline, colors.primary20, shape)
                .background(colors.primary5)
                .testTag(TAG_CRAG_MAP)
                .semantics { contentDescription = mapDescription },
        ) {
            // Previews and JVM screenshot tests cannot load MapLibre's native library: they show the empty
            // map background with the overlays.
            if (!inspectionMode) {
                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    baseStyle = BaseStyle.Uri(if (colors.isDark) STYLE_DARK else STYLE_LIGHT),
                    cameraState = cameraState,
                    zoomRange = MIN_ZOOM.toFloat()..MAX_ZOOM.toFloat(),
                    // Attribution is the caption under the map: no built-in ornaments over the design's controls.
                    options = MapOptions(
                        gestureOptions = GestureOptions.RotationLocked,
                        ornamentOptions = OrnamentOptions.AllDisabled,
                    ),
                ) {
                    MapPins(crag = crag, parking = parking, settori = settori)
                }
            }
            MapChip(text = nome, modifier = Modifier.align(Alignment.TopStart))
            ZoomControls(
                onZoomIn = { scope.launch { cameraState.zoomBy(1.0) } },
                onZoomOut = { scope.launch { cameraState.zoomBy(-1.0) } },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(FalesiaTheme.spacing.space3),
            )
            MapButton(
                icon = R.drawable.ic_my_location,
                contentDescription = stringResource(R.string.cd_recenter),
                onClick = { scope.launch { cameraState.fitTo(points) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(FalesiaTheme.spacing.space3),
                shape = CircleShape,
                tint = colors.primaryText,
            )
            parkingLabel?.let {
                MapChip(
                    text = it,
                    modifier = Modifier.align(Alignment.BottomStart),
                    dotColor = colors.info
                )
            }
        }
        Text(
            text = stringResource(R.string.map_attribution),
            style = FalesiaTheme.typography.caption,
            color = colors.inkMuted,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/** Map layers: sector dots, the parking "P" and the crag pin with its halo, from in-memory GeoJSON. */
@Composable
private fun MapPins(crag: GeoPoint, parking: GeoPoint?, settori: List<GeoPoint>) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    if (settori.isNotEmpty()) {
        val source = rememberGeoJsonSource(GeoJsonData.JsonString(pointsGeoJson(settori)))
        CircleLayer(
            id = "settori",
            source = source,
            color = const(primary),
            radius = const(SectorRadius),
            strokeColor = const(onPrimary),
            strokeWidth = const(FalesiaTheme.size.hairline),
        )
    }
    parking?.let {
        val source = rememberGeoJsonSource(GeoJsonData.JsonString(pointsGeoJson(listOf(it))))
        CircleLayer(
            id = "parking",
            source = source,
            color = const(FalesiaTheme.colors.info),
            radius = const(ParkingRadius),
        )
        SymbolLayer(
            id = "parking-label",
            source = source,
            textField = const(PARKING_LETTER).cast(),
            textFont = const(listOf(MAP_FONT)),
            textColor = const(Color.White),
            textSize = const(12.sp),
            textAllowOverlap = const(true),
            textIgnorePlacement = const(true),
        )
    }
    val cragSource = rememberGeoJsonSource(GeoJsonData.JsonString(pointsGeoJson(listOf(crag))))
    CircleLayer(
        id = "crag-halo",
        source = cragSource,
        color = const(primary.copy(alpha = HALO_ALPHA)),
        radius = const(CragHaloRadius),
    )
    CircleLayer(
        id = "crag",
        source = cragSource,
        color = const(primary),
        radius = const(CragRadius),
        strokeColor = const(onPrimary),
        strokeWidth = const(PinStroke),
    )
}

/** A GeoJSON FeatureCollection of points; coordinates are written with a dot whatever the locale. */
internal fun pointsGeoJson(points: List<GeoPoint>): String =
    points.joinToString(prefix = """{"type":"FeatureCollection","features":[""", postfix = "]}") {
        String.format(
            Locale.ROOT,
            """{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[%f,%f]}}""",
            it.longitude,
            it.latitude,
        )
    }

private fun GeoPoint.toPosition() = Position(latitude = latitude, longitude = longitude)

private suspend fun CameraState.zoomBy(delta: Double) {
    animateTo(position.copy(zoom = (position.zoom + delta).coerceIn(MIN_ZOOM, MAX_ZOOM)))
}

/** Fits all [points]; a single point is centered at [INITIAL_ZOOM]. */
private suspend fun CameraState.fitTo(points: List<GeoPoint>) {
    if (points.size < 2) {
        points.firstOrNull()
            ?.let { animateTo(position.copy(target = it.toPosition(), zoom = INITIAL_ZOOM)) }
        return
    }
    animateTo(
        boundingBox = BoundingBox(
            west = points.minOf { it.longitude },
            south = points.minOf { it.latitude },
            east = points.maxOf { it.longitude },
            north = points.maxOf { it.latitude },
        ),
        padding = PaddingValues(FitPadding),
    )
}

@Composable
private fun MapChip(text: String, modifier: Modifier = Modifier, dotColor: Color? = null) {
    Row(
        modifier = modifier
            .padding(FalesiaTheme.spacing.space3)
            .background(FalesiaTheme.colors.bg, PillShape)
            .border(FalesiaTheme.size.hairline, FalesiaTheme.colors.primary20, PillShape)
            .padding(
                horizontal = FalesiaTheme.spacing.space2,
                vertical = FalesiaTheme.spacing.space1
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1Half),
    ) {
        dotColor?.let {
            Box(
                Modifier
                    .size(SectorDotSize)
                    .background(it, CircleShape)
            )
        }
        Text(
            text = text,
            style = FalesiaTheme.typography.caption,
            fontWeight = if (dotColor == null) FontWeight.SemiBold else FontWeight.Medium,
            color = if (dotColor == null) FalesiaTheme.colors.ink else FalesiaTheme.colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(FalesiaTheme.radius.lg)
    Column(
        modifier = modifier
            .width(ControlSize)
            .clip(shape)
            .background(FalesiaTheme.colors.bg)
            .border(FalesiaTheme.size.hairline, FalesiaTheme.colors.primary20, shape),
    ) {
        MapButton(R.drawable.ic_add, stringResource(R.string.cd_zoom_in), onZoomIn)
        HorizontalDivider(
            thickness = FalesiaTheme.size.hairline,
            color = FalesiaTheme.colors.primary20
        )
        MapButton(R.drawable.ic_remove, stringResource(R.string.cd_zoom_out), onZoomOut)
    }
}

@Composable
private fun MapButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    tint: Color = FalesiaTheme.colors.ink,
) {
    // A standalone (round) button has its own border; zoom buttons share the border of their column.
    val border = if (shape == CircleShape) {
        Modifier.border(FalesiaTheme.size.hairline, FalesiaTheme.colors.primary20, shape)
    } else {
        Modifier
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(ControlSize)
            .then(border)
            .semantics { this.contentDescription = contentDescription },
        shape = shape,
        color = FalesiaTheme.colors.bg,
        contentColor = tint,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(ControlIconSize)
            )
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun CragMapPreview() {
    AppTheme {
        CragMap(
            nome = "Monsummano",
            crag = GeoPoint(43.8695, 10.8273),
            parking = GeoPoint(43.8663, 10.8280),
            settori = listOf(GeoPoint(43.8726, 10.8229)),
            parkingLabel = "Parking · 3–28 min walk",
            modifier = Modifier.padding(16.dp),
        )
    }
}
