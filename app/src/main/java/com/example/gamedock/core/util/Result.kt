package com.example.gamedock.core.util

/**
 * Lightweight Result wrapper for repository responses.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val throwable: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}
