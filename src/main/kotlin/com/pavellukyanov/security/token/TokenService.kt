package com.pavellukyanov.security.token

interface TokenService {
    suspend fun generate(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String
}