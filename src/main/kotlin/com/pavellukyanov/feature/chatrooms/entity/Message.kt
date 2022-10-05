package com.pavellukyanov.feature.chatrooms.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Message(
    @Contextual val id: Id<Message> = newId(),
    val chatroomId: String,
    val messageTimeStamp: Long,
    val ownerId: String,
    val ownerUsername: String,
    val ownerAvatar: String?,
    val message: String
)
