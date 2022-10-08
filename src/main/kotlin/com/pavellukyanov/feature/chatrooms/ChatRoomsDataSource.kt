package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import com.pavellukyanov.feature.chatrooms.entity.Message

interface ChatRoomsDataSource {
    suspend fun insertChatroom(chatroom: Chatroom): Boolean
    suspend fun getAllChatrooms(): List<Chatroom>
    suspend fun updateChatroom(chatroom: Chatroom): Boolean
    suspend fun insertMessages(messages: Message): Boolean
    suspend fun getMessages(chatroomId: String): List<Message>
    suspend fun updateUserAvatar(ownerId: String, avatar: String): Boolean
}