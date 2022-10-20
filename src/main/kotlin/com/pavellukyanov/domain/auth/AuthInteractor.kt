package com.pavellukyanov.domain.auth

import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.auth.entity.Token
import com.pavellukyanov.domain.auth.entity.User
import com.pavellukyanov.domain.users.entity.request.SignInRequest
import com.pavellukyanov.domain.users.entity.request.SignUpRequest
import com.pavellukyanov.domain.users.entity.response.TokenResponse
import com.pavellukyanov.security.token.TokenClaim
import com.pavellukyanov.security.token.TokenConfig
import com.pavellukyanov.security.token.TokenService
import com.pavellukyanov.utils.Errors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AuthInteractor(
    private val tokenService: TokenService,
    private val tokenConfig: TokenConfig,
    private val userDataSource: UserDataSource
) {

    suspend fun signUp(request: SignUpRequest): State<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val userWithEmail = userDataSource.getUserByEmail(request.email)
            val userWithUsername = userDataSource.getUserByEmail(request.username)
            val areFieldsBlank = request.username.isBlank() || request.password.isBlank()

            when {
                userWithEmail != null -> State.Error(Errors.ALREADY_EMAIL)
                userWithUsername != null -> State.Error(Errors.ALREADY_USERNAME)
                areFieldsBlank -> State.Error(Errors.EMPTY_USERNAME_OR_PASSWORD)
                else -> {
                    val newUser = User(
                        username = request.username,
                        password = request.password,
                        email = request.email,
                        avatar = "https://cdn0.iconfinder.com/data/icons/communication-456/24/account_profile_user_contact_person_avatar_placeholder-1024.png"
                    )
                    val token = tokenService.generate(
                        config = tokenConfig,
                        TokenClaim(
                            name = "userId",
                            value = newUser.id.toString()
                        )
                    )
                    val refreshToken = UUID.randomUUID().toString()
                    val isUserInsert = userDataSource.insertUser(newUser)
                    if (isUserInsert) {
                        userDataSource.insertToken(
                            Token(
                                userId = newUser.id.toString(),
                                refreshToken = refreshToken
                            )
                        )
                    }
                    val response = TokenResponse(
                        token = token,
                        refreshToken = refreshToken,
                        message = null
                    )
                    State.Success(response)
                }
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun signIn(request: SignInRequest): State<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val user = userDataSource.getUserByEmail(request.email)
            val isValidPassword = request.password == user?.password

            when {
                user == null -> State.Error(Errors.INCORRECT_EMAIL)
                !isValidPassword -> State.Error(Errors.INCORRECT_PASSWORD)
                else -> {
                    var refreshToken = userDataSource.getRefreshToken(user.id.toString())

                    if (refreshToken == null) {
                        val newRefreshToken = UUID.randomUUID().toString()
                        userDataSource.insertToken(
                            Token(
                                userId = user.id.toString(),
                                refreshToken = newRefreshToken
                            )
                        )
                        refreshToken = newRefreshToken
                    }

                    val token = tokenService.generate(
                        config = tokenConfig,
                        TokenClaim(
                            name = "userId",
                            value = user.id.toString()
                        )
                    )
                    val response = TokenResponse(
                        token = token,
                        refreshToken = refreshToken,
                        message = null
                    )
                    State.Success(response)
                }
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun updateToken(request: String): State<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            when (val userId = userDataSource.getUserIdFromTokens(request)) {
                null -> State.Error(Errors.BAD_REFRESH_TOKEN)
                else -> {
                    val newRefreshToken = UUID.randomUUID().toString()

                    userDataSource.updateToken(
                        Token(
                            userId = userId,
                            refreshToken = newRefreshToken
                        )
                    )

                    val token = tokenService.generate(
                        config = tokenConfig,
                        TokenClaim(
                            name = "userId",
                            value = userId
                        )
                    )

                    val response = TokenResponse(
                        token = token,
                        refreshToken = newRefreshToken,
                        message = null
                    )
                    State.Success(response)
                }
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun logout(userId: String?): State<Boolean> = withContext(Dispatchers.IO) {
        try {
            val state = userDataSource.deleteToken(userId)
            if (userId == null) {
                State.Error(Errors.BAD_USER_ID)
            } else {
                State.Success(state)
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }
}