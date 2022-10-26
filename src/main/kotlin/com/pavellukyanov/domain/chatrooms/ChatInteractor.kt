package com.pavellukyanov.domain.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.domain.SocketMessage
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.auth.entity.User
import com.pavellukyanov.domain.chatrooms.entity.Member
import com.pavellukyanov.domain.chatrooms.entity.Message
import com.pavellukyanov.utils.MemberAlreadyExistsException
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatInteractor(
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

    suspend fun sendMessage(socketMessage: SocketMessage, user: User) {
        val timeStamp = Calendar.getInstance().time.time
        val newChatroom = chatRoomsDataSource.getChatroom(socketMessage.chatRoomId)?.copy(
            lastMessageTimeStamp = timeStamp,
            lastMessage = socketMessage.chatMessage,
            lastMessageOwnerUsername = user.username
        )

        newChatroom?.let {
            chatRoomsDataSource.updateChatroom(it)
        }

        val messageEntity = Message(
            chatroomId = socketMessage.chatRoomId,
            messageTimeStamp = timeStamp,
            ownerId = user.id.toString(),
            ownerUsername = user.username,
            ownerAvatar = user.avatar,
            message = socketMessage.chatMessage
        )

        val isInsert = chatRoomsDataSource.insertMessages(messageEntity)

        if (isInsert) {
            members.values.forEach { member ->
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

    suspend fun getAllMessages(chatroomId: String): State<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val messages = chatRoomsDataSource.getMessages(chatroomId)
            State.Success(messages)
        } catch (e: Exception) {
            State.Exception(e)
        }
    }
}