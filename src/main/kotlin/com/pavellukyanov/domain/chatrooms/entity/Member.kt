package com.pavellukyanov.domain.chatrooms.entity

import com.pavellukyanov.domain.auth.entity.User
import io.ktor.websocket.*

data class Member(
    val user: User,
    val socket: WebSocketSession
)
