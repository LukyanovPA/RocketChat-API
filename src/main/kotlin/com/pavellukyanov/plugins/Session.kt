package com.pavellukyanov.plugins

import com.pavellukyanov.domain.chatrooms.entity.SessionItem
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application.configureSession() {
    install(Sessions) {
        cookie<SessionItem>("SESSION")
    }

    intercept(Plugins) {
        if (call.sessions.get<SessionItem>() == null) {
            call.sessions.set(SessionItem(generateNonce()))
        }
    }
}