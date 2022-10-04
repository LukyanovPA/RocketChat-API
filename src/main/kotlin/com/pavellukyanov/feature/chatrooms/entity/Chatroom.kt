package com.pavellukyanov.feature.chatrooms.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Chatroom(
    @SerializedName("id") val id: Int,
    @SerializedName("ownerUid") val ownerUid: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("chatroomImg") val chatroomImg: String?,
    @SerializedName("lastMessageTimeStamp") val lastMessageTimeStamp: Long,
    @SerializedName("lastMessage") val lastMessage: String
)
