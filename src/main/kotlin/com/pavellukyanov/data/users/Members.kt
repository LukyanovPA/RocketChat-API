package com.pavellukyanov.data.users

import com.pavellukyanov.feature.auth.entity.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.update
import java.util.*

object Members : Table() {
    private val uuid = Members.varchar("uuid", 75)
    private val salt = Members.varchar("salt", 75)
    private val password = Members.varchar("password", 75)
    private val username = Members.varchar("username", 25)
    private val email = Members.varchar("email", 50)
    private val avatar = Members.varchar("avatar", 75)

    suspend fun insert(user: User): User? {
        return suspendedTransactionAsync(Dispatchers.IO) {
            val userModel = Members.insert {
                it[username] = user.username
                it[password] = user.password
                it[salt] = user.salt
                it[email] = user.email
                it[avatar] = user.avatar ?: ""
            }.resultedValues?.firstOrNull()

            if (userModel != null) {
                User(
                    username = userModel[username],
                    password = userModel[password],
                    email = userModel[Members.email],
                    salt = userModel[salt],
                    avatar = userModel[avatar]
                )
            } else {
                null
            }
        }.await()
    }

    suspend fun fetchUser(email: String): User? {
        return suspendedTransactionAsync(Dispatchers.IO) {
            val userModel = Members.select { Members.email eq email }.firstOrNull()
            if (userModel != null) {
                User(
                    username = userModel[username],
                    password = userModel[password],
                    email = userModel[Members.email],
                    salt = userModel[salt],
                    avatar = userModel[avatar]
                )
            } else {
                null
            }
        }.await()
    }

    suspend fun fetchEmail(email: String): Boolean {
        return suspendedTransactionAsync(Dispatchers.IO) {
            val userModel = Members.select { Members.email eq email }.firstOrNull()
            userModel != null
        }.await()
    }

    suspend fun fetchUsername(username: String): Boolean {
        return suspendedTransactionAsync(Dispatchers.IO) {
            val userModel = Members.select { Members.username eq username }.firstOrNull()
            userModel != null
        }.await()
    }

    suspend fun getUser(uuidIn: String): User? =
        suspendedTransactionAsync(Dispatchers.IO) {
            val userModel = Members.select { uuid.eq(uuidIn) }.firstOrNull()
            if (userModel != null) {
                User(
                    username = userModel[username],
                    password = userModel[password],
                    email = userModel[email],
                    salt = userModel[salt],
                    avatar = userModel[avatar]
                )
            } else {
                null
            }
        }.await()

    suspend fun changeAvatar(uuidIn: String, avatarIn: String) {
        newSuspendedTransaction(Dispatchers.IO) {
            Members.update({ uuid eq uuidIn }) {
                it[avatar] = avatarIn
            }
        }
    }
}