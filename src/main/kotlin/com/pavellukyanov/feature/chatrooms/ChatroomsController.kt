package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.chatrooms.ChatInteractor
import com.pavellukyanov.domain.chatrooms.CreateChatRoomInteractor
import com.pavellukyanov.domain.chatrooms.entity.ChatSession
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.utils.MemberAlreadyExistsException
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.bson.types.ObjectId
import java.io.File
import java.util.*

fun Route.createChatRoom(
    createChatRoomInteractor: CreateChatRoomInteractor
) {
    authenticate {
        post("api/chatrooms/create") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = principal.getClaim("userId", String::class)
            val multipartData = call.receiveMultipart()

            var chatRoomName: String? = null
            var chatRoomDescription: String? = null
            var imgPath: String? = null
            var img: String? = null

            try {
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
                    call.respond(HttpStatusCode.BadRequest, "Chat name not specified")
                    return@post
                } else {
                    val chatroom = Chatroom(
                        ownerId = userId.toString(),
                        name = chatRoomName!!,
                        description = chatRoomDescription ?: "",
                        chatroomImg = img ?: "https://alenka.capital/data/preview/583/58348.jpg",
                        imagePath = imgPath,
                        lastMessageTimeStamp = Calendar.getInstance().time.time,
                        lastMessage = "Hi everyone, im create a $chatRoomName!"
                    )

                    call.respond(
                        status = HttpStatusCode.OK,
                        message = createChatRoomInteractor.create(chatroom)
                    )
                }
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}

fun Route.getAllChatrooms(chatRoomsDataSource: ChatRoomsDataSource) {
    authenticate {
        get("api/chatrooms/getAllChatrooms") {
            try {
                val chats = chatRoomsDataSource.getAllChatrooms()
                call.respond(status = HttpStatusCode.OK, message = chats)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}

fun Route.getMessages(chatRoomsDataSource: ChatRoomsDataSource) {
    authenticate {
        get("api/chatrooms/getMessages") {
            try {
                val chatroomId = call.request.queryParameters["chatroomId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val messages = chatRoomsDataSource.getMessages(chatroomId)
                call.respond(status = HttpStatusCode.OK, message = messages)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}

fun Route.sendMessage(
    chatInteractor: ChatInteractor,
    userDataSource: UserDataSource
) {
    authenticate {
        webSocket("api/chat/send/{id?}") {
            val session = call.sessions.get<ChatSession>()
            val principal = call.principal<JWTPrincipal>()
            if (session == null || principal == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
                return@webSocket
            }

            val userId = principal.getClaim("userId", ObjectId::class)

            val user = userDataSource.getCurrentUser(userId!!) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            val chatRoomId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }

            try {
                chatInteractor.onJoin(user = user, socket = this)

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        chatInteractor.sendMessage(
                            chatRoomId = chatRoomId,
                            message = frame.readText(),
                            user = user
                        )
                    }
                }
            } catch (e: MemberAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } finally {
                chatInteractor.tryDisconnect(user)
            }
        }
    }
}

fun Route.deleteChatRoom(chatRoomsDataSource: ChatRoomsDataSource) {
    authenticate {
        post("api/chatrooms/delete/{chatroomId?}") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val chatRoomId = call.parameters["chatroomId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = principal.getClaim("userId", ObjectId::class)

            try {
                val chatroom = chatRoomsDataSource.getChatroom(chatRoomId)
                val isOwner = chatroom?.ownerId == userId.toString()

                if (isOwner) {
                    chatRoomsDataSource.deleteChatroom(chatRoomId).also { state ->
                        chatroom?.imagePath?.let { path ->
                            File("/var/www/html/uploads/chats/$path").delete()
                        }
                        if (state) call.respond(HttpStatusCode.OK, true) else call.respond(HttpStatusCode.Conflict)
                    }
                } else {
                    call.respond(HttpStatusCode.Conflict, "This user is not the chat owner")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            }
        }
    }
}