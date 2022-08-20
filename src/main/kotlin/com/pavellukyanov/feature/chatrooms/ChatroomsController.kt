package com.pavellukyanov.feature.chatrooms

import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.createChatroomStepOne() {
    authenticate {
        post("api/chatrooms/createChatroomStepOne") {

        }
    }
}