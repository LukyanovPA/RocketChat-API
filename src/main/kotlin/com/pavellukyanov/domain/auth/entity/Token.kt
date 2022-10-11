package com.pavellukyanov.domain.auth.entity

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val userId: String,
    val refreshToken: String
)