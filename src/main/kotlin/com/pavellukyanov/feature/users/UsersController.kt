package com.pavellukyanov.feature.users

import com.pavellukyanov.data.users.response.UserResponse
import com.pavellukyanov.feature.auth.UserDataSource
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

fun Route.changeAvatar(userDataSource: UserDataSource) {
    authenticate {
        post("api/users/changeAvatar") {
            try {
                val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                var fileName = ""
                val userId = principal.getClaim("userId", ObjectId::class)
                val user = userDataSource.getCurrentUser(userId!!)

                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName as String
                            var fileBytes = part.streamProvider().readBytes()
                            File("/var/www/html/uploads/avatars/$userId-$fileName").writeBytes(fileBytes)
                        }
                        else -> {}
                    }
                }

                val changedUser = user?.copy(
                    avatar = "http://188.225.9.194/uploads/avatars/$userId-$fileName"
                )!!

                val isAvatarChanged = userDataSource.changeUserAvatar(changedUser)

                call.respond(status = HttpStatusCode.OK, message = isAvatarChanged)
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }
        }
    }
}

fun Route.getCurrentUser(userDataSource: UserDataSource) {
    authenticate {
        get("api/users/currentUser") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", ObjectId::class)
            val user = userDataSource.getCurrentUser(userId!!)

            call.respond(
                status = HttpStatusCode.OK,
                message = UserResponse(
                    uuid = user?.id.toString(),
                    username = user?.username,
                    email = user?.email,
                    avatar = user?.avatar
                )
            )
        }
    }
}