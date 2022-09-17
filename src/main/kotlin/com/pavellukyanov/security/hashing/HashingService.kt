package com.pavellukyanov.security.hashing

interface HashingService {
    suspend fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash
    suspend fun verify(value: String, saltedHash: SaltedHash): Boolean
}