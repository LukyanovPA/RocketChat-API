package com.pavellukyanov.feature.users

import com.pavellukyanov.domain.BaseResponse
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.users.UsersInteractor
import com.pavellukyanov.domain.users.entity.response.UserResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.changeAvatar(
    usersInteractor: UsersInteractor
) {
    authenticate {
        post("api/users/changeAvatar") {
            val principal = call.principal<JWTPrincipal>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = principal.getClaim("userId", ObjectId::class)
            val multipartData = call.receiveMultipart()

            when (val state = usersInteractor.changeAvatar(userId, multipartData)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard UserResponse>(
                            success = true,
                            data = state.data
                        )
                    )
                    return@post
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard UserResponse>(
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

fun Route.getCurrentUser(usersInteractor: UsersInteractor) {
    authenticate {
        get("api/users/currentUser") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", ObjectId::class)

            when (val state = usersInteractor.getCurrentUser(userId)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard UserResponse>(
                            success = true,
                            data = state.data
                        )
                    )
                    return@get
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard UserResponse>(
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

fun Route.getAllUsers(usersInteractor: UsersInteractor) {
    authenticate {
        get("api/users/getAllUsers") {
            when (val state = usersInteractor.getAllUsers()) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard List<UserResponse>>(
                            success = true,
                            data = state.data
                        )
                    )
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard List<UserResponse>>(
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