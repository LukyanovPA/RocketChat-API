package com.pavellukyanov.domain

import kotlinx.serialization.Serializable

@Serializable
data class SocketMessage(
    val chatMessage: String,
    val chatRoomId: String
)