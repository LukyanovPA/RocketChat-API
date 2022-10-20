package com.pavellukyanov

import com.pavellukyanov.plugins.*
import com.pavellukyanov.security.token.TokenConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.inject

fun main() {
    embeddedServer(Netty, port = 8080) {

        val jwtConfig by inject<TokenConfig>()

        configureKoin()
        configureWebSockets()
        configureDoubleReceive()
        configureSession()
        configureSerialization()
        configureMonitoring()
        configureSecurity(jwtConfig)
        configureRouting()
    }.start(wait = true)
}
