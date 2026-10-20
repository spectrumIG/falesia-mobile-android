package it.softsamurai.domain.usecase

import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.domain.sync.DownloadModeProvider
import javax.inject.Inject

/** Whether screens download on their own or show the manual download button (see [DownloadMode]). */
class GetDownloadModeUseCase @Inject constructor(
    private val provider: DownloadModeProvider,
) {
    operator fun invoke(): DownloadMode = provider.current()
}
