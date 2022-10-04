package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom

interface ChatRoomsDataSource {
    suspend fun insert(chatroom: Chatroom): Boolean
    suspend fun getAllChatrooms(): List<Chatroom>
}