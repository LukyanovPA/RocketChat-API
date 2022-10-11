package com.pavellukyanov.domain.chatrooms.entity

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Message(
    @BsonId val id: String = ObjectId().toString(),
    val chatroomId: String,
    val messageTimeStamp: Long,
    val ownerId: String,
    val ownerUsername: String,
    val ownerAvatar: String?,
    val message: String
)
