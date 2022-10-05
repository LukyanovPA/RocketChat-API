package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import com.pavellukyanov.feature.chatrooms.entity.UploadChatImgResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

fun Route.createChatroom(chatRoomsDataSource: ChatRoomsDataSource) {
    authenticate {
        post("api/chatrooms/createChatroom") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", ObjectId::class)
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

                    try {
                        val isInsert = chatRoomsDataSource.insert(chatroom)
                        call.respond(status = HttpStatusCode.OK, message = isInsert)
                    } catch (e: Exception) {
                        call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
                    }
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