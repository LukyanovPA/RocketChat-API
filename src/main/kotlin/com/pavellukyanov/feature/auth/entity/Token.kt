package com.pavellukyanov.feature.auth.entity

data class Token(
    val uuid: String,
    val refreshToken: String
)