package com.pavellukyanov.data.users

import com.pavellukyanov.feature.auth.entity.User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    private val uuid = Users.uuid("uuid")
    private val salt = Users.varchar("salt", 75)
    private val password = Users.varchar("password", 75)
    private val username = Users.varchar("username", 30)
    private val email = Users.varchar("email", 25)

    fun insert(user: User) {
        transaction {
            Users.insert {
                it[username] = user.username
                it[password] = user.password
                it[email] = user.email
                it[uuid] = user.uuid
                it[salt] = user.salt
            }
        }
    }

    fun fetchUser(username: String): User? {
        return try {
            transaction {
                val userModel = Users.select { Users.username.eq(username) }.single()
                User(
                    username = userModel[Users.username],
                    password = userModel[password],
                    email = userModel[email],
                    uuid = userModel[uuid],
                    salt = userModel[salt]
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}