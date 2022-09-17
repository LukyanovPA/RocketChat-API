package com.pavellukyanov.data.users

import com.pavellukyanov.data.users.response.TokenResponse
import com.pavellukyanov.feature.auth.entity.User
import com.pavellukyanov.security.hashing.HashingService
import com.pavellukyanov.security.token.TokenClaim
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*

class UserRepo(
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
) {
//    fun isValidEmail(email: String): Boolean = Users.fetchEmail(email)

//    fun isValidUsername(username: String): Boolean = Users.fetchUsername(username)

    @OptIn(FlowPreview::class)
    suspend fun insert(username: String, email: String, password: String): Flow<TokenResponse> =
        flowOf(hashingService.generateSaltedHash(password))
            .flowOn(Dispatchers.Default)
            .flatMapMerge { saltedHash ->
                flow {
                    val uuid = UUID.randomUUID()

                    Users.insert(
                        User(
                            username = username,
                            password = saltedHash.hash,
                            email = email,
                            uuid = uuid,
                            salt = saltedHash.salt,
                            avatar = null
                        )
                    )

                    emit(uuid)
                }.flowOn(Dispatchers.IO)
            }.flatMapMerge { uuid ->
                flow {
                    val token = tokenService.generate(
                        config = tokenConfig,
                        TokenClaim(
                            name = "userUUID",
                            value = uuid.toString()
                        )
                    )
                    emit(token to uuid)
                }.flowOn(Dispatchers.Default)
            }.flatMapMerge { response ->
                flow {
                    val refreshToken = UUID.randomUUID().toString()

                    Tokens.insert(
                        uuidIn = response.second.toString(),
                        refreshTokenIn = refreshToken
                    )
                    emit(response.first to refreshToken)
                }.flowOn(Dispatchers.IO)
            }.flatMapMerge { response ->
                flow {
                    emit(
                        TokenResponse(
                            token = response.first,
                            refreshToken = response.second,
                            message = null
                        )
                    )
                }
            }
}