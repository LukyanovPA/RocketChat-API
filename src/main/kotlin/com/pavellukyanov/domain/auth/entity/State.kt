package com.pavellukyanov.domain.auth.entity

sealed class State<out T> {
    data class Success<out T>(val data: T) : State<T>()
    data class Error(val error: String) : State<Nothing>()
    data class Exception(val exception: Throwable) : State<Nothing>()
}
