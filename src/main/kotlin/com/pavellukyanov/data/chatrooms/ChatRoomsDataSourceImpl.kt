package com.pavellukyanov.data.chatrooms

import com.pavellukyanov.feature.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.toList

class ChatRoomsDataSourceImpl(
    db: CoroutineDatabase
) : ChatRoomsDataSource {
    private val chatrooms = db.getCollection<Chatroom>()

    override suspend fun insert(chatroom: Chatroom): Boolean =
        chatrooms.insertOne(chatroom).wasAcknowledged()

    override suspend fun getAllChatrooms(): List<Chatroom> =
        chatrooms.collection.find().toList()
}