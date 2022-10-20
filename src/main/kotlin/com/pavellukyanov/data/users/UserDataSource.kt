package com.pavellukyanov.data.users

import com.pavellukyanov.domain.auth.entity.Token
import com.pavellukyanov.domain.auth.entity.User
import org.bson.types.ObjectId

interface UserDataSource {
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun getCurrentUser(id: ObjectId): User?
    suspend fun changeUserAvatar(user: User): Boolean
    suspend fun insertToken(token: Token): Boolean
    suspend fun getUserIdFromTokens(refreshTokenIn: String): String?
    suspend fun getRefreshToken(userId: String): String?
    suspend fun updateToken(token: Token): Boolean
    suspend fun deleteToken(userId: String?): Boolean
    suspend fun getAllUsers(): List<User>
}