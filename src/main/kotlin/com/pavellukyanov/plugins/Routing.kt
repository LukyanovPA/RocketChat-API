package com.pavellukyanov.plugins

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.auth.AuthInteractor
import com.pavellukyanov.domain.chatrooms.ChatInteractor
import com.pavellukyanov.domain.chatrooms.ChatRoomInteractor
import com.pavellukyanov.domain.users.UsersInteractor
import com.pavellukyanov.feature.auth.logout
import com.pavellukyanov.feature.auth.refreshToken
import com.pavellukyanov.feature.auth.signIn
import com.pavellukyanov.feature.auth.signUp
import com.pavellukyanov.feature.chatrooms.*
import com.pavellukyanov.feature.users.changeAvatar
import com.pavellukyanov.feature.users.getAllUsers
import com.pavellukyanov.feature.users.getCurrentUser
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userDataSource by inject<UserDataSource>()
    val chatRoomsDataSource by inject<ChatRoomsDataSource>()
    val chatInteractor by inject<ChatInteractor>()
    val createChatInteractor by inject<ChatRoomInteractor>()
    val authInteractor by inject<AuthInteractor>()
    val usersInteractor by inject<UsersInteractor>()

    routing {
        route("/") {
            //Auth
            signUp(authInteractor)
            signIn(authInteractor)
            refreshToken(authInteractor)
            logout(authInteractor)

            //Users
            changeAvatar(usersInteractor)
            getCurrentUser(usersInteractor)
            getAllUsers(usersInteractor)

            //Chatrooms
            getAllChatrooms(chatRoomsDataSource)
            getMessages(chatInteractor)
            sendMessage(chatInteractor, userDataSource)
            deleteChatRoom(chatRoomsDataSource)
            createChatRoom(createChatInteractor)
        }
    }
}
