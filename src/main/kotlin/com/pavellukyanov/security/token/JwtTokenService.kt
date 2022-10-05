package com.pavellukyanov.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class JwtTokenService : TokenService {
    override suspend fun generate(config: TokenConfig, vararg claims: TokenClaim): String =
        withContext(Dispatchers.IO) {
            var token = JWT.create()
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            claims.forEach { claim ->
                token = token.withClaim(claim.name, claim.value)
            }
            token.sign(Algorithm.HMAC256(config.secret))
        }
}