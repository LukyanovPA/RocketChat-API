package com.pavellukyanov.plugins

import com.pavellukyanov.domain.chatrooms.entity.ChatSession
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application.configureSession() {
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    intercept(Plugins) {
        if(call.sessions.get<ChatSession>() == null) {
            val username = call.parameters["userId"] ?: "defaultId"
            call.sessions.set(ChatSession(username, generateNonce()))
        }
    }
}