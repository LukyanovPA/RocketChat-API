package com.pavellukyanov.feature.users

import com.pavellukyanov.data.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.changeAvatar() {
    authenticate {
        post("api/users/changeAvatar") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = principal.getClaim("userUUID", String::class)
            val request = call.request.queryParameters["avatar"]

            if (request == null) {
                call.respond(status = HttpStatusCode.BadRequest, message = "No avatar link")
                return@post
            } else {
                Users.changeAvatar(UUID.fromString(userId), request)
                call.respond(status = HttpStatusCode.OK, message = true)
            }
        }
    }
}