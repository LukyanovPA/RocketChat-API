package com.pavellukyanov.plugins

import com.pavellukyanov.feature.auth.*
import com.pavellukyanov.feature.chatrooms.*
import com.pavellukyanov.feature.users.changeAvatar
import com.pavellukyanov.feature.users.getCurrentUser
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userDataSource: UserDataSource,
    chatRoomsDataSource: ChatRoomsDataSource
) {
    routing {
//        trace { application.log.trace(it.buildText()) }
        //Auth
        route("/") {
            signUp(tokenService, tokenConfig, userDataSource)
            signIn(tokenService, tokenConfig, userDataSource)
            refreshToken(tokenService, tokenConfig, userDataSource)
            logout(userDataSource)
            info()

            //Users
            changeAvatar(userDataSource)
            getCurrentUser(userDataSource)

            //Chatrooms
            uploadChatRoomImg()
            createChatroom(chatRoomsDataSource, userDataSource)
            getAllChatrooms(chatRoomsDataSource)
            getMessages(chatRoomsDataSource)
            sendMessage(chatRoomsDataSource, userDataSource)
        }
    }
}
