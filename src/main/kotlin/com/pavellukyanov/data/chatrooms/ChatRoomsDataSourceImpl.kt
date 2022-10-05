package com.pavellukyanov.data.chatrooms

import com.pavellukyanov.feature.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import com.pavellukyanov.feature.chatrooms.entity.Message
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.toList

class ChatRoomsDataSourceImpl(
    db: CoroutineDatabase
) : ChatRoomsDataSource {
    private val chatrooms = db.getCollection<Chatroom>()
    private val messages = db.getCollection<Message>()

    override suspend fun insertChatroom(chatroom: Chatroom): Boolean =
        chatrooms.insertOne(chatroom).wasAcknowledged()

    override suspend fun getAllChatrooms(): List<Chatroom> =
        chatrooms.collection.find().toList().sortedByDescending { it.lastMessageTimeStamp }

    override suspend fun insertMessages(messages: Message): Boolean =
        this.messages.insertOne(messages).wasAcknowledged()

    override suspend fun getMessages(chatroomId: String): List<Message> =
        messages.find().toList().filter { it.chatroomId == chatroomId }
}