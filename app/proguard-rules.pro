# R8 rules for the release build (minify + shrink + obfuscation).
#
# Libraries ship their own consumer rules (Hilt/Dagger, WorkManager + hilt-work, Room, kotlinx-serialization,
# Coil, OkHttp, MapLibre, Compose, lifecycle): do not repeat them here. Add a rule only for code the app
# reaches by reflection, with a comment saying why, and check it on a release build (doc/tech-stack.md).

# Readable crash stack traces: keep line numbers, hide the source file names. Retrace obfuscated traces with
# build/outputs/mapping/release/mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Navigation 3 saves the back stack by class name (NavKeySerializer: getClass().getName(), then
# Class.forName + serializer(KClass) on restore). The destinations must keep their names, must not be merged
# with other classes and keep their generated serializers.
-keep class it.softsamurai.presentation.navigation.AppRoute$* { *; }
