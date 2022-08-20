package com.pavellukyanov.data.chatrooms

import com.pavellukyanov.feature.chatrooms.entity.Chatroom
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Chatrooms : Table() {
    private val id = Chatrooms.integer("id")
    private val ownerUid = Chatrooms.varchar("ownerUid", 1000)
    private val name = Chatrooms.varchar("name", 30)
    private val description = Chatrooms.varchar("description", 100)
    private val chatroomImg = Chatrooms.varchar("chatroomImg", 400)
    private val lastMessageTimeStamp = Chatrooms.long("lastMessageTimeStamp")
    private val lastMessage = Chatrooms.varchar("lastMessage", 1000)

    fun insert(chatroom: Chatroom) {
        transaction {
            Chatrooms.insert {
                it[id] = chatroom.id
                it[ownerUid] = chatroom.ownerUid
                it[name] = chatroom.name
                it[description] = chatroom.description
                chatroom.chatroomImg?.let { img -> it[chatroomImg] = img }
                it[lastMessageTimeStamp] = chatroom.lastMessageTimeStamp
                it[lastMessage] = chatroom.lastMessage
            }
        }
    }
}