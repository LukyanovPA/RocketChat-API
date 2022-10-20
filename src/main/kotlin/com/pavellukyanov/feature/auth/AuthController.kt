package com.pavellukyanov.feature.auth

import com.pavellukyanov.domain.BaseResponse
import com.pavellukyanov.domain.auth.AuthInteractor
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.users.entity.request.SignInRequest
import com.pavellukyanov.domain.users.entity.request.SignUpRequest
import com.pavellukyanov.domain.users.entity.response.TokenResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    authInteractor: AuthInteractor
) {
    post("api/auth/signup") {
        val request = call.receiveNullable<SignUpRequest>()

        if (request != null) {
            when (val state = authInteractor.signUp(request)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
                            success = true,
                            data = state.data
                        )
                    )
                    return@post
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
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
        } else {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
    }
}

fun Route.signIn(
    authInteractor: AuthInteractor
) {
    post("api/auth/signin") {
        val request = call.receiveNullable<SignInRequest>()

        if (request != null) {
            when (val state = authInteractor.signIn(request)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
                            success = true,
                            data = state.data
                        )
                    )
                    return@post
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
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
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

fun Route.refreshToken(
    authInteractor: AuthInteractor
) {
    post("api/auth/updateToken") {
        val request = call.request.queryParameters["refreshToken"]

        if (request == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        } else {
            when (val state = authInteractor.updateToken(request)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
                            success = true,
                            data = state.data
                        )
                    )
                    return@post
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<@JvmWildcard TokenResponse>(
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

fun Route.logout(authInteractor: AuthInteractor) {
    authenticate {
        get("api/auth/logout") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            when (val state = authInteractor.logout(userId)) {
                is State.Success -> {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = BaseResponse(
                            success = true,
                            data = state.data
                        )
                    )
                    return@get
                }
                is State.Error -> {
                    call.respond(
                        status = HttpStatusCode.ServiceUnavailable,
                        message = BaseResponse<Boolean>(
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