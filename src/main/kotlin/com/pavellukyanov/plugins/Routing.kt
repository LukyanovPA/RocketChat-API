package com.pavellukyanov.plugins

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.chatrooms.ChatInteractor
import com.pavellukyanov.domain.chatrooms.CreateChatRoomInteractor
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
    val chatInteractor by inject<ChatInteractor>()
    val createChatInteractor by inject<CreateChatRoomInteractor>()

    routing {
//        trace { application.log.trace(it.buildText()) }
        route("/") {
            //Auth
            signUp(tokenService, tokenConfig, userDataSource)
            signIn(tokenService, tokenConfig, userDataSource)
            refreshToken(tokenService, tokenConfig, userDataSource)
            logout(userDataSource)
            info()

            //Users
            changeAvatar(chatRoomsDataSource, userDataSource)
            getCurrentUser(userDataSource)

            //Chatrooms
            getAllChatrooms(chatRoomsDataSource)
            getMessages(chatRoomsDataSource)
            sendMessage(chatInteractor, userDataSource)
            deleteChatRoom(chatRoomsDataSource)
            createChatRoom(createChatInteractor)
        }
    }
}
