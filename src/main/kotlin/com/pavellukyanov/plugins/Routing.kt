package com.pavellukyanov.plugins

import com.pavellukyanov.feature.auth.getSecretInfo
import com.pavellukyanov.feature.auth.signIn
import com.pavellukyanov.feature.auth.signUp
import com.pavellukyanov.security.hashing.HashingService
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        signUp(hashingService, tokenService, tokenConfig)
        signIn(hashingService, tokenService, tokenConfig)
        getSecretInfo()
    }
}
