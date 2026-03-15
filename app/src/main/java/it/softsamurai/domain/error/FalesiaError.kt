package it.softsamurai.domain.error

sealed interface FalesiaError {
    data class NotFound(val id: String) : FalesiaError
    data class DatabaseError(val cause: Throwable) : FalesiaError
    data object UnknownError : FalesiaError
}
