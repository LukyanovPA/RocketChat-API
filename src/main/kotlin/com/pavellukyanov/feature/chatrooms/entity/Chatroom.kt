package com.pavellukyanov.feature.chatrooms.entity

data class Chatroom(
    val id: Int,
    val ownerUid: String,
    val name: String,
    val description: String,
    val chatroomImg: String?,
    val lastMessageTimeStamp: Long,
    val lastMessage: String
)
