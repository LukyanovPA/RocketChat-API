package com.pavellukyanov.data.users.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerializedName("uuid") val uuid: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatar") val avatar: String?
)
