package com.pavellukyanov.feature.auth

import com.pavellukyanov.data.users.request.SignInRequest
import com.pavellukyanov.data.users.request.SignUpRequest
import com.pavellukyanov.data.users.response.TokenResponse
import com.pavellukyanov.feature.auth.entity.Token
import com.pavellukyanov.feature.auth.entity.User
import com.pavellukyanov.security.token.TokenClaim
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

fun Route.signUp(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userDataSource: UserDataSource
) {
    post("api/auth/signup") {
        val request = call.receiveNullable<SignUpRequest>()

        if (request != null) {
            val user = userDataSource.getUserByEmail(request.email)
            val areFieldsBlank = request.username.isBlank() || request.password.isBlank()

            if (user != null) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = "A user with this email already exists"
                )
                return@post
            }

            if (areFieldsBlank) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = "Empty field username or password"
                )
                return@post
            }

            try {
                val newUser = User(
                    username = request.username,
                    password = request.password,
                    email = request.email,
                    avatar = null
                )
                val token = tokenService.generate(
                    config = tokenConfig,
                    TokenClaim(
                        name = "userId",
                        value = newUser.id.toString()
                    )
                )
                val refreshToken = UUID.randomUUID().toString()
                val isUserInsert = userDataSource.insertUser(newUser)
                if (isUserInsert) {
                    userDataSource.insertToken(
                        Token(
                            userId = newUser.id.toString(),
                            refreshToken = refreshToken
                        )
                    )
                }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = TokenResponse(
                        token = token,
                        refreshToken = refreshToken,
                        message = null
                    )
                )

            } catch (e: Exception) {
                call.respond(status = HttpStatusCode.Conflict, message = e.localizedMessage)
            }

        } else {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
    }
}

fun Route.signIn(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userDataSource: UserDataSource
) {
    post("api/auth/signin") {
        val request = call.receiveNullable<SignInRequest>()

        if (request != null) {
            val user = userDataSource.getUserByEmail(request.email)

            if (user == null) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = "Incorrect email or password"
                )
                return@post
            }

            val isValidPassword = request.password == user.password

            if (!isValidPassword) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = "Incorrect email or password"
                )
                return@post
            }

            var refreshToken = withContext(Dispatchers.IO) {
                userDataSource.getRefreshToken(user.id.toString())
            }

            if (refreshToken == null) {
                val newRefreshToken = UUID.randomUUID().toString()
                withContext(Dispatchers.IO) {
                    userDataSource.insertToken(
                        Token(
                            userId = user.id.toString(),
                            refreshToken = newRefreshToken
                        )
                    )
                }
                refreshToken = newRefreshToken
            }

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = user.id.toString()
                )
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = TokenResponse(
                    token = token,
                    refreshToken = refreshToken,
                    message = null
                )
            )
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

fun Route.refreshToken(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userDataSource: UserDataSource
) {
    post("api/auth/updateToken") {
        val request = call.request.queryParameters["refreshToken"]

        if (request == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        } else {
            val userId = withContext(Dispatchers.IO) {
                userDataSource.getUserIdFromTokens(request)
            }

            if (userId == null) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = TokenResponse(
                        token = null,
                        refreshToken = null,
                        message = "Bad Refresh Token"
                    )
                )
                return@post
            } else {
                val newRefreshToken = UUID.randomUUID().toString()

                withContext(Dispatchers.IO) {
                    userDataSource.updateToken(
                        Token(
                            userId = userId,
                            refreshToken = newRefreshToken
                        )
                    )
                }

                val token = withContext(Dispatchers.IO) {
                    tokenService.generate(
                        config = tokenConfig,
                        TokenClaim(
                            name = "userId",
                            value = userId
                        )
                    )
                }

                call.respond(
                    status = HttpStatusCode.OK,
                    message = TokenResponse(
                        token = token,
                        refreshToken = newRefreshToken,
                        message = null
                    )
                )
            }
        }
    }
}

fun Route.logout(userDataSource: UserDataSource) {
    authenticate {
        get("api/auth/logout") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            val state = withContext(Dispatchers.IO) { userDataSource.deleteToken(userId) }

            if (state) call.respond(HttpStatusCode.OK, true) else call.respond(HttpStatusCode.Conflict)
        }
    }
}

fun Route.info() {
    get("api/hello") {
        call.respond(status = HttpStatusCode.OK, message = "Hello World")
    }
}