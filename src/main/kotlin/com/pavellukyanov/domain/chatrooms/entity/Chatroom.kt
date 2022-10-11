package com.pavellukyanov.domain.chatrooms.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
@Serializable
data class Chatroom(
    @Contextual val id: Id<Chatroom> = newId(),
    val ownerId: String,
    val name: String,
    val description: String,
    val chatroomImg: String?,
    val lastMessageTimeStamp: Long,
    val lastMessage: String
)
