package com.pavellukyanov.di

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.chatrooms.ChatRoomsDataSourceImpl
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.data.users.UserDataSourceImpl
import com.pavellukyanov.domain.auth.AuthInteractor
import com.pavellukyanov.domain.chatrooms.ChatInteractor
import com.pavellukyanov.domain.chatrooms.CreateChatRoomInteractor
import com.pavellukyanov.domain.users.UsersInteractor
import com.pavellukyanov.security.token.JwtTokenService
import com.pavellukyanov.security.token.TokenService
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


val mainModule = module {
    single {
        val mongoPw = System.getenv("BD_PASSWORD")
        val bdUser = System.getenv("BD_USER")
        val dbName = "RocketChat"
        KMongo.createClient(
            connectionString = "mongodb+srv://$bdUser:$mongoPw@$bdUser.vkgwyn9.mongodb.net/?retryWrites=true&w=majority"
        ).coroutine
            .getDatabase(dbName)
    }

    single {
        UsersInteractor(get(), get())
    }

    single {
        AuthInteractor(get(), get(), get())
    }

    single<ChatRoomsDataSource> {
        ChatRoomsDataSourceImpl(get())
    }

    single<UserDataSource> {
        UserDataSourceImpl(get())
    }

    single<TokenService> {
        JwtTokenService()
    }

    single {
        ChatInteractor(get())
    }

    single {
        CreateChatRoomInteractor(get(), get())
    }
}