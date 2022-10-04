package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import java.util.*

fun Route.createChatroom(chatRoomsDataSource: ChatRoomsDataSource) {
    authenticate {
        post("api/chatrooms/createChatroom") {
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
                    ownerUid = userId.toString(),
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