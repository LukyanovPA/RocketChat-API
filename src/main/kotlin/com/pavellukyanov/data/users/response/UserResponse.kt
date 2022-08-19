package com.pavellukyanov.data.users.response

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val uuid: String?,
    val username: String?,
    val email: String?
)
