package com.pavellukyanov.plugins

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.chatrooms.ChatRoomInteractor
import com.pavellukyanov.feature.auth.*
import com.pavellukyanov.feature.chatrooms.*
import com.pavellukyanov.feature.users.changeAvatar
import com.pavellukyanov.feature.users.getCurrentUser
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(
    tokenConfig: TokenConfig,
) {
    val userDataSource by inject<UserDataSource>()
    val chatRoomsDataSource by inject<ChatRoomsDataSource>()
    val tokenService by inject<TokenService>()
    val chatRoomInteractor by inject<ChatRoomInteractor>()

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
            changeAvatar(chatRoomsDataSource, userDataSource)
            getCurrentUser(userDataSource)

            //Chatrooms
            uploadChatRoomImg()
            createChatroom(chatRoomsDataSource, userDataSource)
            getAllChatrooms(chatRoomsDataSource)
            getMessages(chatRoomsDataSource)
            sendMessage(chatRoomsDataSource, userDataSource)
            sendMessageWebSocket(chatRoomInteractor, userDataSource)
        }
    }
}
