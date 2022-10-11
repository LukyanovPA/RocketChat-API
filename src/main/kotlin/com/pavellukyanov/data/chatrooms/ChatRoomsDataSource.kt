package com.pavellukyanov.data.chatrooms

import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message

interface ChatRoomsDataSource {
    suspend fun insertChatroom(chatroom: Chatroom): Boolean
    suspend fun getAllChatrooms(): List<Chatroom>
    suspend fun updateChatroom(chatroom: Chatroom): Boolean
    suspend fun insertMessages(messages: Message): Boolean
    suspend fun getMessages(chatroomId: String): List<Message>
    suspend fun updateUserAvatar(ownerId: String, avatar: String): Boolean
}