package com.pavellukyanov.feature.auth.entity

import java.util.UUID

data class User(
    val uuid: UUID,
    val username: String,
    val password: String,
    val email: String,
    val salt: String,
    val avatar: String?
)
