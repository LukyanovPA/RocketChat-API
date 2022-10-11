package com.pavellukyanov.domain.users.entity.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshToken(
    val refreshToken: String
)
