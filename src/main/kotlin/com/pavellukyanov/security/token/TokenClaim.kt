package com.pavellukyanov.security.token

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TokenClaim(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
