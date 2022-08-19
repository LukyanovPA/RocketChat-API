package com.pavellukyanov.data.users.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshToken(
    val refreshToken: String
)
