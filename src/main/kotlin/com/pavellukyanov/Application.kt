package com.pavellukyanov

import com.pavellukyanov.plugins.*
import com.pavellukyanov.security.token.TokenConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        val jwtConfig = TokenConfig(
            issuer = environment.config.config("jwt.issuer").toString(),
            audience = environment.config.config("jwt.audience").toString(),
            expiresIn = 3600000L, //60 minutes
            secret = System.getenv("JWT_SECRET")
        )

        configureKoin()
        configureWebSockets()
        configureDoubleReceive()
        configureSession()
        configureSerialization()
        configureMonitoring()
        configureSecurity(jwtConfig)
        configureRouting(jwtConfig)
    }.start(wait = true)
}
