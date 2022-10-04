package com.pavellukyanov.data.users.request

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
