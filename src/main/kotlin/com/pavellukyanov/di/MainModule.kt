package com.pavellukyanov.di

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSourceImpl
import com.pavellukyanov.data.users.UserDataSourceImpl
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.domain.chatrooms.ChatRoomInteractor
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
        ChatRoomInteractor(get())
    }
}