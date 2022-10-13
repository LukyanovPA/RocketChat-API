package com.pavellukyanov.domain.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message
import org.bson.types.ObjectId

class CreateChatRoomInteractor(
    private val chatRoomsDataSource: ChatRoomsDataSource,
    private val userDataSource: UserDataSource
) {
    suspend fun create(chatRoom: Chatroom): Boolean {
        chatRoomsDataSource.insertChatroom(chatRoom)
        val user = userDataSource.getCurrentUser(ObjectId(chatRoom.ownerId))!!
        return chatRoomsDataSource.insertMessages(
            Message(
                chatroomId = chatRoom.id.toString(),
                messageTimeStamp = chatRoom.lastMessageTimeStamp,
                ownerId = chatRoom.ownerId,
                ownerUsername = user.username,
                ownerAvatar = user.avatar!!,
                message = chatRoom.lastMessage
            )
        )
    }
}