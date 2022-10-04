package com.pavellukyanov.data.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.pavellukyanov.feature.auth.UserDataSource
import com.pavellukyanov.feature.auth.entity.Token
import com.pavellukyanov.feature.auth.entity.User
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserDataSourceImpl(
    db: CoroutineDatabase
) : UserDataSource {
    private val users = db.getCollection<User>()
    private val tokens = db.getCollection<Token>()

    override suspend fun getUserByEmail(email: String): User? =
        users.findOne(User::email eq email)

    override suspend fun insertUser(user: User): Boolean =
        users.insertOne(user).wasAcknowledged()

    override suspend fun getCurrentUser(id: ObjectId): User? =
        users.findOne(User::id eq id)

    override suspend fun changeUserAvatar(id: ObjectId, avatarIn: String): Boolean =
        users.updateOne(Filters.eq("id", id), Updates.set("avatar", avatarIn)).wasAcknowledged()

    override suspend fun insertToken(token: Token): Boolean =
        tokens.insertOne(token).wasAcknowledged()

    override suspend fun getUserIdFromTokens(refreshTokenIn: String): String? =
        tokens.findOne(Token::refreshToken eq refreshTokenIn)?.userId

    override suspend fun getRefreshToken(userId: String): String? =
        tokens.findOne(Token::userId eq userId)?.refreshToken

    override suspend fun updateToken(token: Token): Boolean =
        tokens.updateOne(Filters.eq("userId", token.userId), Updates.set("refreshToken", token.refreshToken))
            .wasAcknowledged()

    override suspend fun deleteToken(userId: String?): Boolean =
        tokens.deleteOne(Filters.eq("userId", userId)).wasAcknowledged()
}