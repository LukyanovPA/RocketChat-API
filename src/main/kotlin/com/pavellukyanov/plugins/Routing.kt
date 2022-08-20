package com.pavellukyanov.plugins

import com.pavellukyanov.feature.auth.*
import com.pavellukyanov.feature.users.changeAvatar
import com.pavellukyanov.security.hashing.HashingService
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {

    routing {
        //Auth
        signUp(hashingService, tokenService, tokenConfig)
        signIn(hashingService, tokenService, tokenConfig)
        refreshToken(tokenService, tokenConfig)
        getSecretInfo()
        logout()

        //Users
        changeAvatar()
    }
}
