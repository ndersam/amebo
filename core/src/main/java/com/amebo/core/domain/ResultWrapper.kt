package com.amebo.core.domain

sealed class ResultWrapper<out T, out U>{
    data class Success<out T>(val data: T): ResultWrapper<T, Nothing>()
    data class Failure<out U>(val data: U): ResultWrapper<Nothing, U>()

    val isSuccess get() = this is Success
    val isFailure get() = this is Failure



    fun <R> either(onSuccess: (Success<T>) -> R, onFailure: (Failure<U>) -> R): R{
        return if(this is Success)
            return onSuccess(this)
        else onFailure(this as Failure<U>)
    }

    val asSuccess get() = this as Success<T>

    companion object {
        fun <T> success(t: T) = Success(t)
        fun <U> failure(u: U) = Failure(u)
    }
}
