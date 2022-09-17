package com.pavellukyanov.data.users

import com.pavellukyanov.feature.auth.entity.User
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Users : Table() {
    private val uuid = Users.uuid("uuid")
    private val salt = Users.varchar("salt", 75)
    private val password = Users.varchar("password", 75)
    private val username = Users.varchar("username", 25)
    private val email = Users.varchar("email", 50)
    private val avatar = Users.varchar("avatar", 400)

    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        transaction {
            Users.insert {
                it[username] = user.username
                it[password] = user.password
                it[email] = user.email
                it[uuid] = user.uuid
                it[salt] = user.salt
                it[avatar] = user.avatar ?: ""
            }
        }
    }

    fun fetchUser(email: String): User? {
        return try {
            transaction {
                val userModel = Users.select { Users.email.eq(email) }.single()
                User(
                    username = userModel[username],
                    password = userModel[password],
                    email = userModel[Users.email],
                    uuid = userModel[uuid],
                    salt = userModel[salt],
                    avatar = userModel[avatar]
                )
            }
        } catch (e: Exception) {
            println("fetchUser $e")
            null
        }
    }

    fun fetchEmail(email: String): String? {
        return try {
            transaction {
                val userModel = Users.select { Users.email.eq(email) }.single()
                userModel[Users.email]
            }
        } catch (e: Exception) {
            println("fetchEmail $e")
            null
        }
    }

    fun fetchUsername(username: String): String? {
        return try {
            transaction {
                val userModel = Users.select { Users.username.eq(username) }.single()
                userModel[Users.username]
            }
        } catch (e: Exception) {
            println("fetchUsername $e")
            null
        }
    }

    fun getUser(uuidIn: String): User? = try {
        transaction {
            val userModel = Users.select { uuid.eq(UUID.fromString(uuidIn)) }.single()
            User(
                username = userModel[username],
                password = userModel[password],
                email = userModel[email],
                uuid = userModel[uuid],
                salt = userModel[salt],
                avatar = userModel[avatar]
            )
        }
    } catch (e: Exception) {
        println("getUser $e")
        null
    }

    fun changeAvatar(uuidIn: UUID, avatarIn: String) {
        transaction {
            Users.update({ uuid eq uuidIn }) {
                it[avatar] = avatarIn
            }
        }
    }
}