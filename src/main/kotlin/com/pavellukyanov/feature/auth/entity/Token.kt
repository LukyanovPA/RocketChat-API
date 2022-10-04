package com.pavellukyanov.feature.auth.entity

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val userId: String,
    val refreshToken: String
)