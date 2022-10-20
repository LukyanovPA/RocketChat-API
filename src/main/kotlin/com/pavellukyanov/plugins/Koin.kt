package com.pavellukyanov.plugins

import com.pavellukyanov.di.mainModule
import com.pavellukyanov.security.token.TokenConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        val tokenConfigModule = module {
            single {
                TokenConfig(
                    issuer = environment.config.config("jwt.issuer").toString(),
                    audience = environment.config.config("jwt.audience").toString(),
                    expiresIn = 3600000L, //60 minutes
                    secret = System.getenv("JWT_SECRET")
                )
            }
        }
        slf4jLogger()
        modules(mainModule)
        modules(tokenConfigModule)
    }
}