package com.pavellukyanov.data.users.request

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: String,
    val password: String,
    val email: String
)
