package com.pavellukyanov.feature.chatrooms.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Chatroom(
    @Transient
    @BsonId val id: ObjectId = ObjectId(),
    val ownerUid: String,
    val name: String,
    val description: String,
    val chatroomImg: String?,
    val lastMessageTimeStamp: Long,
    val lastMessage: String
)
