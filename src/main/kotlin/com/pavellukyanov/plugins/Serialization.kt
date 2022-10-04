package com.pavellukyanov.plugins

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.serializersModule
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.text.DateFormat

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json { serializersModule = IdKotlinXSerializationModule }
        )
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
        register(ContentType.Any, GsonConverter())
    }
}
