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
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*

fun Route.signUp(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("api/auth/signUp") {
        val request = call.receiveOrNull<SignUpRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = Users.fetchUser(request.username)
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        val uuid = UUID.randomUUID()

        if (areFieldsBlank || isPwTooShort) call.respond(HttpStatusCode.Conflict)

        if (user != null) {
            call.respond(HttpStatusCode.Conflict, "A user with this username already exists")
        } else {
            try {
                val saltedHash = hashingService.generateSaltedHash(request.password)

                Users.insert(
                    User(
                        username = request.username,
                        password = saltedHash.hash,
                        email = request.email,
                        uuid = uuid,
                        salt = saltedHash.salt
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
                        refreshToken = refreshToken
                    )
                )
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
            }
        }
    }
}

fun Route.signIn(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("api/auth/signIn") {
        val request = call.receiveOrNull<SignInRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = Users.fetchUser(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
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
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val refreshToken = Tokens.getRefreshToken(user.uuid.toString())

        if (refreshToken == null) {
            val newRefreshToken = UUID.randomUUID().toString()
            Tokens.insert(
                uuidIn = user.uuid.toString(),
                refreshTokenIn = newRefreshToken
            )
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
                refreshToken = refreshToken!!
            )
        )
    }
}

fun Route.refreshToken(
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("api/auth/refreshToken") {
        val request = call.receiveText()

        val userUuid = Tokens.getUuid(request)
        if (userUuid == null) {
            call.respond(HttpStatusCode.Conflict, "Bad Refresh Token")
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
                    refreshToken = newRefreshToken
                )
            )
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("api/auth/secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userUUID", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}