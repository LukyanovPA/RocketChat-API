package com.pavellukyanov

import com.pavellukyanov.data.users.UserDataSourceImpl
import com.pavellukyanov.plugins.configureMonitoring
import com.pavellukyanov.plugins.configureRouting
import com.pavellukyanov.plugins.configureSecurity
import com.pavellukyanov.plugins.configureSerialization
import com.pavellukyanov.security.token.JwtTokenService
import com.pavellukyanov.security.token.TokenConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main() {
    embeddedServer(Netty, port = 8080) {
        val mongoPw = System.getenv("BD_PASSWORD")
        val bdUser = System.getenv("BD_USER")
        val dbName = "RocketChat"
        val db = KMongo.createClient(
            connectionString = "mongodb+srv://$bdUser:$mongoPw@$bdUser.vkgwyn9.mongodb.net/?retryWrites=true&w=majority"
        ).coroutine
            .getDatabase(dbName)
        val userDataSource = UserDataSourceImpl(db)
        val tokenService = JwtTokenService()
        val jwtConfig = TokenConfig(
            issuer = environment.config.config("jwt.issuer").toString(),
            audience = environment.config.config("jwt.audience").toString(),
            expiresIn = 3600000L, //60 minutes
            secret = System.getenv("JWT_SECRET")
        )

        configureSerialization()
        configureMonitoring()
        configureSecurity(jwtConfig)
        configureRouting(tokenService, jwtConfig, userDataSource)
    }.start(wait = true)
}
