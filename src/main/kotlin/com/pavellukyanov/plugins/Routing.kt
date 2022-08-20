package com.pavellukyanov.plugins

import com.pavellukyanov.feature.auth.refreshToken
import com.pavellukyanov.feature.auth.signIn
import com.pavellukyanov.feature.auth.signUp
import com.pavellukyanov.feature.users.changeAvatar
import com.pavellukyanov.feature.users.getCurrentUser
import com.pavellukyanov.feature.users.logout
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

        //Users
        changeAvatar()
        getCurrentUser()
        logout()
    }
}
