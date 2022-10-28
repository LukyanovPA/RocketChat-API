package com.pavellukyanov.domain.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message
import com.pavellukyanov.utils.Errors
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import java.io.File
import java.util.*

class ChatRoomInteractor(
    private val chatRoomsDataSource: ChatRoomsDataSource,
    private val userDataSource: UserDataSource
) {
    suspend fun create(multipartData: MultiPartData, userId: ObjectId): State<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = userDataSource.getCurrentUser(userId)!!
            var chatRoomName: String? = null
            var chatRoomDescription: String? = null
            var imgPath: String? = null
            var img: String? = null

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "name") chatRoomName = part.value
                        if (part.name == "description") chatRoomDescription = part.value
                    }
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName as String
                        var fileBytes = part.streamProvider().readBytes()
                        imgPath = "$userId-$fileName"
                        File("/var/www/html/uploads/chats/$imgPath").writeBytes(fileBytes)
                        img = "http://188.225.9.194/uploads/chats/$imgPath"
                    }
                    else -> {}
                }
            }

            if (chatRoomName == null) {
                State.Error(Errors.CHAT_NAME_NOT_SPECIFIED)
            } else {
                val chatRoom = Chatroom(
                    ownerId = userId.toString(),
                    name = chatRoomName!!,
                    description = chatRoomDescription ?: "",
                    chatroomImg = img ?: "https://alenka.capital/data/preview/583/58348.jpg",
                    imagePath = imgPath,
                    lastMessageTimeStamp = Calendar.getInstance().time.time,
                    lastMessage = "Hi everyone, im create a $chatRoomName!",
                    lastMessageOwnerUsername = user.username
                )
                val message = Message(
                    chatroomId = chatRoom.id.toString(),
                    messageTimeStamp = chatRoom.lastMessageTimeStamp,
                    ownerId = chatRoom.ownerId,
                    ownerUsername = user.username,
                    ownerAvatar = user.avatar!!,
                    message = chatRoom.lastMessage
                )

                chatRoomsDataSource.insertChatroom(chatRoom)
                val response = chatRoomsDataSource.insertMessages(message)
                State.Success(response)
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun getAllChatRooms(): State<List<Chatroom>> = withContext(Dispatchers.IO) {
        try {
            State.Success(chatRoomsDataSource.getAllChatrooms())
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun delete(userId: ObjectId, chatRoomId: String): State<Boolean> = withContext(Dispatchers.IO) {
        try {
            val chatroom = chatRoomsDataSource.getChatroom(chatRoomId)
            val isOwner = chatroom?.ownerId == userId.toString()

            if (isOwner) {
                val state = chatRoomsDataSource.deleteChatroom(chatroom!!)
                if (state) {
                    chatroom.imagePath?.let { path ->
                        File("/var/www/html/uploads/chats/$path").delete()
                    }
                }
                State.Success(state)
            } else {
                State.Error(Errors.USER_IS_NOT_CHAT_OWNER)
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }
}