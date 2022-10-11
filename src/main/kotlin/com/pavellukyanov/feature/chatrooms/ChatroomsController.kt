package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.chatrooms.ChatRoomInteractor
import com.pavellukyanov.domain.chatrooms.entity.ChatSession
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message
import com.pavellukyanov.domain.chatrooms.entity.UploadChatImgResponse
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import java.io.File
import java.util.*

fun Route.uploadChatRoomImg() {
    authenticate {
        post("api/chatrooms/uploadChatImg") {
            try {
                val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                var fileName = ""
                val userId = principal.getClaim("userId", String::class)

                val multipartData = call.receiveMultipart()

                launch(Dispatchers.IO) {
                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                fileName = part.originalFileName as String
                                var fileBytes = part.streamProvider().readBytes()
                                File("/var/www/html/uploads/chats/$userId-$fileName").writeBytes(fileBytes)
                            }
                            else -> {}
                        }
                    }
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = UploadChatImgResponse(
                            success = true,
                            src = "http://188.225.9.194/uploads/chats/$userId-$fileName",
                            errorMessage = null
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.Conflict, message = UploadChatImgResponse(
                        success = false,
                        src = null,
                        errorMessage = e.localizedMessage
                    )
                )
            }
        }
    }
}

fun Route.createChatroom(
    chatRoomsDataSource: ChatRoomsDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("api/chatrooms/createChatroom") {
            try {
                val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val userId = principal.getClaim("userId", ObjectId::class)
                val name = call.request.queryParameters["name"]
                val description = call.request.queryParameters["description"]
                val img = call.request.queryParameters["img"]

                if (name == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                } else {
                    val chatroom = Chatroom(
                        ownerId = userId.toString(),
                        name = name,
                        description = description ?: "",
                        chatroomImg = img ?: "https://alenka.capital/data/preview/583/58348.jpg",
                        lastMessageTimeStamp = Calendar.getInstance().time.time,
                        lastMessage = "Hi everyone, im create a $name!"
                    )
                    launch { chatRoomsDataSource.insertChatroom(chatroom) }
                    val user = userDataSource.getCurrentUser(userId)!!
                    val isMessageInsert = chatRoomsDataSource.insertMessages(
                        Message(
                            chatroomId = chatroom.id.toString(),
                            messageTimeStamp = chatroom.lastMessageTimeStamp,
                            ownerId = userId.toString(),
                            ownerUsername = user.username,
                            ownerAvatar = user.avatar!!,
                            message = chatroom.lastMessage
                        )
                    )

                    call.respond(status = HttpStatusCode.OK, message = isMessageInsert)
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
    chatRoomsDataSource: ChatRoomsDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("api/chatrooms/sendMessage") {
            try {
                val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val chatroomId = call.request.queryParameters["chatroomId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val message = call.request.queryParameters["message"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val userId = principal.getClaim("userId", ObjectId::class)
                val timeStamp = Calendar.getInstance().time.time
                val user = userDataSource.getCurrentUser(userId!!)

                launch {
                    val newChatroom = chatRoomsDataSource.getAllChatrooms()
                        .find { it.id.toString() == chatroomId }
                        ?.copy(
                            lastMessageTimeStamp = timeStamp,
                            lastMessage = message
                        )

                    newChatroom?.let { chatRoomsDataSource.updateChatroom(it) }
                }

                val isMessageInsert = chatRoomsDataSource.insertMessages(
                    Message(
                        chatroomId = chatroomId,
                        messageTimeStamp = timeStamp,
                        ownerId = userId.toString(),
                        ownerUsername = user?.username!!,
                        ownerAvatar = user.avatar,
                        message = message
                    )
                )
                call.respond(status = HttpStatusCode.OK, message = isMessageInsert)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}

fun Route.sendMessageWebSocket(
    chatRoomInteractor: ChatRoomInteractor,
    userDataSource: UserDataSource
) {
    webSocket("api/chat/send/{id?}") {
        val session = call.sessions.get<ChatSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }

        val userId = ObjectId(session.userId)

        val user = userDataSource.getCurrentUser(userId) ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@webSocket
        }
        val chatRoomId = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@webSocket
        }

        try {
            chatRoomInteractor.onJoin(
                user = user,
                socket = this
            )

            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    chatRoomInteractor.sendMessage(
                        chatRoomId = chatRoomId,
                        message = frame.readText(),
                        user = user
                    )
                }
            }
        } catch (e: MemberAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            chatRoomInteractor.tryDisconnect(user)
        }
    }
}