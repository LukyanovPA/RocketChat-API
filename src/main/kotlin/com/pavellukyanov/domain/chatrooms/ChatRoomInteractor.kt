package com.pavellukyanov.domain.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.domain.auth.entity.User
import com.pavellukyanov.domain.chatrooms.entity.Member
import com.pavellukyanov.domain.chatrooms.entity.Message
import com.pavellukyanov.utils.MemberAlreadyExistsException
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatRoomInteractor(
    private val chatRoomsDataSource: ChatRoomsDataSource
) {
    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(user: User, socket: WebSocketSession) {
        if (members.containsKey(user.id.toString())) throw MemberAlreadyExistsException()

        members[user.id.toString()] = Member(
            user = user,
            socket = socket
        )
    }

    suspend fun sendMessage(chatRoomId: String, message: String, user: User) {
        members.values.forEach { member ->
            val timeStamp = Calendar.getInstance().time.time
            val newChatroom = chatRoomsDataSource.getAllChatrooms()
                .find { it.id.toString() == chatRoomId }
                ?.copy(
                    lastMessageTimeStamp = timeStamp,
                    lastMessage = message
                )

            newChatroom?.let { chatRoomsDataSource.updateChatroom(it) }

            val messageEntity = Message(
                chatroomId = chatRoomId,
                messageTimeStamp = timeStamp,
                ownerId = user.id.toString(),
                ownerUsername = user.username,
                ownerAvatar = user.avatar,
                message = message
            )

            val isInsert = chatRoomsDataSource.insertMessages(messageEntity)

            if (isInsert) {
                val parsedMessage = Json.encodeToString(messageEntity)
                member.socket.send(Frame.Text(parsedMessage))
            }
        }
    }

    suspend fun tryDisconnect(user: User) {
        members[user.id.toString()]?.socket?.close()
        if (members.containsKey(user.id.toString())) {
            members.remove(user.id.toString())
        }
    }
}