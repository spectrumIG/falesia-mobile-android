package it.softsamurai.domain.error

sealed interface SyncError {
    data class NetworkFailure(val cause: Throwable) : SyncError
    data class ParseFailure(val message: String) : SyncError
    data class StorageFailure(val cause: Throwable) : SyncError

    /** The crag to refresh is not in the local database. */
    data class CragNotFound(val id: String) : SyncError
}
