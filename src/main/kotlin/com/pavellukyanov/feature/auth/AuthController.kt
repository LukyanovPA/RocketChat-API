package com.pavellukyanov.feature.auth

import com.pavellukyanov.data.users.Users
import com.pavellukyanov.data.users.request.SignUpRequest
import com.pavellukyanov.data.users.response.TokenResponse
import com.pavellukyanov.feature.auth.entity.User
import com.pavellukyanov.security.hashing.HashingService
import com.pavellukyanov.security.token.TokenClaim
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*

fun Route.signUp(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signup") {
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

                call.respond(
                    status = HttpStatusCode.OK,
                    message = TokenResponse(
                        token = token
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