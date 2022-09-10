package com.pavellukyanov.feature.chatrooms

import com.pavellukyanov.data.chatrooms.Chatrooms
import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.random.Random

fun Route.createChatroom() {
    authenticate {
        post("api/chatrooms/createChatroom") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userUUID", String::class)
            val name = call.request.queryParameters["name"]
            val description = call.request.queryParameters["description"]
            val img = call.request.queryParameters["img"]

            if (name == null || userId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            } else {
                val id: Int

                while (true) {
                    val tempId = Random.nextInt()
                    val chatId = Chatrooms.fetchChatroom(tempId)
                    if (chatId == null) {
                        id = tempId
                        break
                    }
                }

                val chatroom = Chatroom(
                    id = id,
                    ownerUid = userId,
                    name = name,
                    description = description ?: "",
                    chatroomImg = img ?: "https://alenka.capital/data/preview/583/58348.jpg",
                    lastMessageTimeStamp = Calendar.getInstance().time.time,
                    lastMessage = "Hi everyone, im create a $name!"
                )

                try {
                    Chatrooms.insert(chatroom)
                    call.respond(status = HttpStatusCode.OK, message = true)
                } catch (e: Exception) {
                    call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
                }
            }
        }
    }
}

fun Route.getAllChatrooms() {
    authenticate {
        get("api/chatrooms/getAllChatrooms") {
            try {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = Chatrooms.getAllChatrooms()
                )
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}