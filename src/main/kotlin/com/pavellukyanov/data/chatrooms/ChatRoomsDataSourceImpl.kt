package com.pavellukyanov.data.chatrooms

import com.mongodb.client.model.Filters
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.toList

class ChatRoomsDataSourceImpl(
    db: CoroutineDatabase
) : ChatRoomsDataSource {
    private val chatrooms = db.getCollection<Chatroom>()
    private val messages = db.getCollection<Message>()

    override suspend fun insertChatroom(chatroom: Chatroom): Boolean = withContext(Dispatchers.IO) {
        chatrooms.insertOne(chatroom).wasAcknowledged()
    }

    override suspend fun getAllChatrooms(): List<Chatroom> = withContext(Dispatchers.IO) {
        chatrooms.find().descendingSort(Chatroom::lastMessageTimeStamp).toList()
    }

    override suspend fun updateChatroom(chatroom: Chatroom): Boolean = withContext(Dispatchers.IO) {
        chatrooms.replaceOne(Filters.eq("id", chatroom.id), chatroom).wasAcknowledged()
    }

    override suspend fun insertMessages(messages: Message): Boolean = withContext(Dispatchers.IO) {
        this@ChatRoomsDataSourceImpl.messages.insertOne(messages).wasAcknowledged()
    }

    override suspend fun getMessages(chatroomId: String): List<Message> = withContext(Dispatchers.IO) {
        messages.find().toList().filter { it.chatroomId == chatroomId }
    }

    override suspend fun updateUserAvatar(ownerId: String, avatar: String): Boolean = withContext(Dispatchers.IO) {
        messages.collection.find().toList().filter { it.ownerId == ownerId }.forEach { message ->
            val replace = message.copy(
                ownerAvatar = avatar
            )
            messages.replaceOne(Filters.eq("id", message.id), replace)
        }
        return@withContext true
    }

    override suspend fun getChatroom(chatroomId: String): Chatroom? = withContext(Dispatchers.IO) {
        chatrooms.find().toList().find { it.id.toString() == chatroomId }
    }

    override suspend fun deleteChatroom(chatroomId: String): Boolean = withContext(Dispatchers.IO) {
        chatrooms.deleteOne(Filters.eq("id", chatroomId))
        return@withContext messages.deleteMany(Filters.eq("chatroomId", chatroomId)).wasAcknowledged()
    }
}