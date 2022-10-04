package com.pavellukyanov.data.users.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("message") val message: String?
)
