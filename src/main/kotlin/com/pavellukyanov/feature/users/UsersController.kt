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
                var fileDescription = ""
                var fileName = ""
                val userId = principal.getClaim("userId", String::class)

                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            fileDescription = part.value
                        }

                        is PartData.FileItem -> {
                            fileName = part.originalFileName as String
                            var fileBytes = part.streamProvider().readBytes()
                            File("/home/share/uploads/avatars/$userId-$fileName").writeBytes(fileBytes)
                        }

                        else -> {}
                    }
                }

                call.respondText("$fileDescription is uploaded to '/home/share/uploads/avatars/$userId-$fileName'")
            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }

//            val request = call.request.queryParameters["avatar"]
//
//            if (request == null) {
//                call.respond(status = HttpStatusCode.BadRequest, message = "No avatar link")
//                return@post
//            } else {
//                try {
//                    val insert = userDataSource.changeUserAvatar(ObjectId(userId), request)
//                    call.respond(status = HttpStatusCode.OK, message = insert)
//                } catch (e: Exception) {
//                    call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
//                }
//            }
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