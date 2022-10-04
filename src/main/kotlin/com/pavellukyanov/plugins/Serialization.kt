package com.pavellukyanov.plugins

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.text.DateFormat

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
        register(ContentType.Any, GsonConverter())
    }
}
