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
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val config = HikariConfig("hikari.properties")
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    val tokenService = JwtTokenService()
    val jwtConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSerialization()
    configureMonitoring()
    configureSecurity(jwtConfig)
    configureRouting(hashingService, tokenService, jwtConfig)
}
