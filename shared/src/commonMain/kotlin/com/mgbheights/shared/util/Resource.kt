package com.mgbheights.shared.util

/**
 * A generic sealed class that wraps data with loading/error states.
 * Used throughout the app for representing async operation results.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorMessageOrNull(): String? = (this as? Error)?.message

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, throwable)
        is Loading -> Loading
    }

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(message: String, throwable: Throwable? = null): Resource<Nothing> = Error(message, throwable)
        fun loading(): Resource<Nothing> = Loading
    }
}

