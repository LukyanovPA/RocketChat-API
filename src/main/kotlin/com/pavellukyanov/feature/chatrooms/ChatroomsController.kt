package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.BaseResponse
import com.pavellukyanov.domain.SocketMessage
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.chatrooms.ChatInteractor
import com.pavellukyanov.domain.chatrooms.ChatRoomInteractor
import com.pavellukyanov.domain.chatrooms.entity.Chatroom
import com.pavellukyanov.domain.chatrooms.entity.Message
import com.pavellukyanov.utils.MemberAlreadyExistsException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

fun Route.createChatRoom(
    chatRoomInteractor: ChatRoomInteractor
) {
    authenticate {
        post("api/chatrooms/create") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = principal.getClaim("userId", ObjectId::class)
            val multipartData = call.receiveMultipart()

            when (val state = chatRoomInteractor.create(multipartData, userId!!)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<Boolean>(
                            success = true,
                            data = state.data
                        )
                    )
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<Boolean>(
                            success = false,
                            errorMessage = state.error
                        )
                    )
                    return@post
                }
                is State.Exception -> call.respond(
                    status = HttpStatusCode.Conflict,
                    message = state.exception.localizedMessage
                )
            }
        }
    }
}

fun Route.getAllChatrooms(chatRoomInteractor: ChatRoomInteractor) {
    authenticate {
        get("api/chatrooms/getAllChatrooms") {
            when (val state = chatRoomInteractor.getAllChatRooms()) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard List<Chatroom>>(
                            success = true,
                            data = state.data
                        )
                    )
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard List<Chatroom>>(
                            success = false,
                            errorMessage = state.error
                        )
                    )
                    return@get
                }
                is State.Exception -> call.respond(
                    status = HttpStatusCode.Conflict,
                    message = state.exception.localizedMessage
                )
            }
        }
    }
}

fun Route.getMessages(chatInteractor: ChatInteractor) {
    authenticate {
        get("api/chatrooms/getMessages") {
            val chatroomId = call.request.queryParameters["chatroomId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            when (val state = chatInteractor.getAllMessages(chatroomId)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard List<Message>>(
                            success = true,
                            data = state.data
                        )
                    )
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard List<Message>>(
                            success = false,
                            errorMessage = state.error
                        )
                    )
                    return@get
                }
                is State.Exception -> call.respond(
                    status = HttpStatusCode.Conflict,
                    message = state.exception.localizedMessage
                )
            }
        }
    }
}

fun Route.sendMessage(
    chatInteractor: ChatInteractor,
    userDataSource: UserDataSource
) {
    authenticate {
        webSocket("api/chat/send") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            val userId = principal.getClaim("userId", ObjectId::class)
            val user = userDataSource.getCurrentUser(userId!!) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            try {
                chatInteractor.onJoin(user = user, socket = this)
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val socketMessage = Json.decodeFromString<SocketMessage>(frame.readText())
                        chatInteractor.sendMessage(
                            socketMessage = socketMessage,
                            user = user
                        )
                    }
                }
            } catch (e: MemberAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, this.extensions)
            } finally {
                chatInteractor.tryDisconnect(user)
            }
        }
    }
}

fun Route.deleteChatRoom(chatRoomInteractor: ChatRoomInteractor) {
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

            when (val state = chatRoomInteractor.delete(userId!!, chatRoomId)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<Boolean>(
                            success = true,
                            data = state.data
                        )
                    )
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<Boolean>(
                            success = false,
                            errorMessage = state.error
                        )
                    )
                    return@post
                }
                is State.Exception -> call.respond(
                    status = HttpStatusCode.Conflict,
                    message = state.exception.localizedMessage
                )
            }
        }
    }
}