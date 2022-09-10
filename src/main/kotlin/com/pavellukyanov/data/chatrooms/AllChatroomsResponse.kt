package com.pavellukyanov.data.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom

@kotlinx.serialization.Serializable
data class AllChatroomsResponse(
    val chats: List<Chatroom>
)
