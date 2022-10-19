package com.pavellukyanov.utils

import com.pavellukyanov.domain.auth.entity.User
import com.pavellukyanov.domain.users.entity.response.UserResponse

fun User.map(): UserResponse =
    UserResponse(
        uuid = id.toString(),
        username = username,
        email = null,
        avatar = avatar
    )