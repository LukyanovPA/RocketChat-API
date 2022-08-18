package com.pavellukyanov

import com.pavellukyanov.plugins.configureMonitoring
import com.pavellukyanov.plugins.configureRouting
import com.pavellukyanov.plugins.configureSecurity
import com.pavellukyanov.plugins.configureSerialization
import com.pavellukyanov.security.hashing.SHA256HashingService
import com.pavellukyanov.security.token.JwtTokenService
import com.pavellukyanov.security.token.TokenConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

fun main() {
    val config = HikariConfig("hikari.properties")
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    embeddedServer(Netty, port = System.getenv("PORT").toInt()) {
        val tokenService = JwtTokenService()
        val jwtConfig = TokenConfig(
            issuer = environment.config.config("jwt.issuer").toString(),
            audience = environment.config.config("jwt.audience").toString(),
            expiresIn = 365L * 1000L * 60L * 60L * 24L,
            secret = environment.config.config("jwt.secret").toString()
        )
        val hashingService = SHA256HashingService()

        configureSerialization()
        configureMonitoring()
        configureSecurity(jwtConfig)
        configureRouting(hashingService, tokenService, jwtConfig)
    }.start(wait = true)
}
