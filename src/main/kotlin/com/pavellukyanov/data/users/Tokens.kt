package com.pavellukyanov.data.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens : Table() {
    private val uuid = Tokens.varchar("uuid", 1000)
    private val refreshToken = Tokens.varchar("refreshToken", 1000)

    fun insert(uuidIn: String, refreshTokenIn: String) = try {
        transaction {
            Tokens.insert {
                it[uuid] = uuidIn
                it[refreshToken] = refreshTokenIn
            }
        }
    } catch (e: java.lang.Exception) {
        println("insert $e")
    }

    fun getUuid(refreshTokenIn: String): String? =
        try {
            transaction {
                val token = Tokens.select { refreshToken.eq(refreshTokenIn) }.single()
                token[uuid]
            }
        } catch (e: Exception) {
            println("getUuid $e")
            null
        }

    fun getRefreshToken(uuidIn: String): String? =
        try {
            transaction {
                val uuidLocal = Tokens.select { uuid.eq(uuidIn) }.single()
                uuidLocal[refreshToken]
            }
        } catch (e: Exception) {
            println("getRefreshToken $e")
            null
        }

    fun updateToken(uuidIn: String, newRefreshToken: String) {
        try {
            transaction {
                Tokens.update({ uuid eq uuidIn }) {
                    it[refreshToken] = newRefreshToken
                }
            }
        } catch (e: Exception) {
            println("updateToken $e")
        }
    }

    fun deleteToken(uuidIn: String?): Boolean =
        try {
            if (uuidIn != null) {
                transaction {
                    Tokens.deleteWhere { uuid eq uuidIn }
                    true
                }
            } else {
                false
            }
        } catch (e: Exception) {
            println("deleteToken $e")
            false
        }
}