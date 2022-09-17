package com.pavellukyanov.feature.auth

import com.pavellukyanov.data.users.Tokens
import com.pavellukyanov.data.users.Users
import com.pavellukyanov.data.users.request.SignInRequest
import com.pavellukyanov.data.users.request.SignUpRequest
import com.pavellukyanov.data.users.response.TokenResponse
import com.pavellukyanov.feature.auth.entity.User
import com.pavellukyanov.security.hashing.HashingService
import com.pavellukyanov.security.hashing.SaltedHash
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
import kotlinx.coroutines.FlowPreview
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*

@OptIn(FlowPreview::class)
fun Route.signUp(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
//    userRepo: UserRepo
) {
    post("api/auth/signUp") {
        val request = kotlin.runCatching { call.receiveNullable<SignUpRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val email = Users.fetchEmail(request.email)
        val username = Users.fetchUsername(request.username)
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        val uuid = UUID.randomUUID()

        if (areFieldsBlank || isPwTooShort) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Empty field username or password"
            )
            return@post
        }

        if (email != null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "A user with this email already exists"
            )
            return@post
        } else if (username != null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "A user with this username already exists"
            )
            return@post
        }

        try {
            val saltedHash = hashingService.generateSaltedHash(request.password)

            Users.insert(
                User(
                    username = request.username,
                    password = saltedHash.hash,
                    email = request.email,
                    uuid = uuid,
                    salt = saltedHash.salt,
                    avatar = null
                )
            )

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userUUID",
                    value = uuid.toString()
                )
            )

            val refreshToken = UUID.randomUUID().toString()

            Tokens.insert(
                uuidIn = uuid.toString(),
                refreshTokenIn = refreshToken
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = TokenResponse(
                    token = token,
                    refreshToken = refreshToken,
                    message = null
                )
            )
            return@post
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            return@post
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
            return@post
        }
    }

//        flow {
//            val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
//            val isPwTooShort = request.password.length < 8
//
//            emit(areFieldsBlank to isPwTooShort)
//        }.flowOn(Dispatchers.Default)
//            .flatMapMerge { response ->
//                flow {
//                    if (response.first || response.second) {
//                        call.respond(
//                            status = HttpStatusCode.Conflict,
//                            message = "Empty field username or password"
//                        )
//                        emit(false)
//                    } else {
//                        emit(true)
//                    }
//                }
//            }.flatMapMerge { state ->
//                flow {
//                    if (state) {
//                        userRepo.isValidEmail(request.email).also { isValid ->
//                            if (!isValid) call.respond(
//                                status = HttpStatusCode.Conflict,
//                                message = "A user with this email already exists"
//                            )
//                            emit(isValid)
//                        }
//                    } else {
//                        emit(false)
//                    }
//                }.flowOn(Dispatchers.IO)
//            }.flatMapMerge { state ->
//                flow {
//                    if (state) {
//                        userRepo.isValidUsername(request.username).also { isValid ->
//                                if (!isValid) call.respond(
//                                        status = HttpStatusCode.Conflict,
//                                        message = "A user with this username already exists"
//                                    )
//                                emit(isValid)
//                            }
//                    } else {
//                        emit(false)
//                    }
//                }.flowOn(Dispatchers.IO)
//            }
//            .flatMapMerge { state ->
//                flow {
//                    if (state) {
//                        userRepo.insert(request.username, request.email, request.password)
//                            .collect { response ->
//                                call.respond(
//                                    status = HttpStatusCode.OK,
//                                    message = response
//                                )
//                                emit(true)
//                            }
//                    } else {
//                        emit(false)
//                    }
//                }
//            }.catch { e ->
//                when (e) {
//                    is ExposedSQLException -> call.respond(HttpStatusCode.Conflict, e.localizedMessage)
//                    is Exception -> call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
//                }
//            }.collect { state ->
//                if (!state) call.respond(HttpStatusCode.BadRequest)
//            }

//        userRepo.isValidEmail(request.email)
//            .collect { state ->
//                if (!state) {
//                    call.respond(
//                        status = HttpStatusCode.Conflict,
//                        message = "A user with this email already exists"
//                    )
//                }
//            }
//
//        userRepo.isValidUsername(request.username)
//            .collect { state ->
//                if (!state) {
//                    call.respond(
//                        status = HttpStatusCode.Conflict,
//                        message = "A user with this username already exists"
//                    )
//                }
//            }

//        userRepo.insert(request.username, request.email, request.password)
//            .catch { e ->
//                when (e) {
//                    is ExposedSQLException -> call.respond(HttpStatusCode.Conflict, e.localizedMessage)
//                    is Exception -> call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
//                }
//            }
//            .collect { response ->
//                call.respond(
//                    status = HttpStatusCode.OK,
//                    message = response
//                )
//            }
//    }
}

fun Route.signIn(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("api/auth/signIn") {
        val request = kotlin.runCatching { call.receiveNullable<SignInRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = Users.fetchUser(request.email)

        if (user == null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Incorrect email or password"
            )
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Incorrect email or password"
            )
            return@post
        }

        var refreshToken = Tokens.getRefreshToken(user.uuid.toString())

        if (refreshToken == null) {
            val newRefreshToken = UUID.randomUUID().toString()
            Tokens.insert(
                uuidIn = user.uuid.toString(),
                refreshTokenIn = newRefreshToken
            )
            refreshToken = newRefreshToken
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userUUID",
                value = user.uuid.toString()
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
    }
}

fun Route.refreshToken(
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("api/auth/updateToken") {
        val request = call.request.queryParameters["refreshToken"]

        if (request == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        } else {
            val userUuid = Tokens.getUuid(request)
            if (userUuid == null) {
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

                Tokens.updateToken(
                    uuidIn = userUuid,
                    newRefreshToken = newRefreshToken
                )

                val token = tokenService.generate(
                    config = tokenConfig,
                    TokenClaim(
                        name = "userUUID",
                        value = userUuid
                    )
                )

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

fun Route.logout() {
    authenticate {
        get("api/auth/logout") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userUUID", String::class)
            val state = Tokens.deleteToken(userId)

            if (state) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.Conflict)
        }
    }
}

fun Route.info() {
    get("api/hello") {
        call.respond(status = HttpStatusCode.OK, message = "Hello World")
    }
}