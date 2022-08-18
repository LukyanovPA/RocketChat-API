package com.pavellukyanov.data.users

import io.ktor.server.plugins.callloging.*
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens : Table() {
    private val uuid = Tokens.varchar("uuid", 1000)
    private val refreshToken = Tokens.varchar("refreshToken", 1000)

    fun insert(uuidIn: String, refreshTokenIn: String) {
        transaction {
            Tokens.insert {
                it[uuid] = uuidIn
                it[refreshToken] = refreshTokenIn
            }
        }
    }

    fun getUuid(refreshTokenIn: String): String? =
        try {
            transaction {
                val token = Tokens.select { refreshToken.eq(refreshTokenIn) }.single()
                token[uuid]
            }
        } catch (e: Exception) {
            throw e
        }

    fun getRefreshToken(uuidIn: String): String? =
        try {
            transaction {
                val uuidLocal = Tokens.select { uuid.eq(uuidIn) }.single()
                uuidLocal[refreshToken]
            }
        } catch (e: Exception) {
            throw e
        }

    fun updateToken(uuidIn: String, newRefreshToken: String) {
        try {
            transaction {
                Tokens.insert {
                    it[uuid] = uuidIn
                    it[refreshToken] = newRefreshToken
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
}