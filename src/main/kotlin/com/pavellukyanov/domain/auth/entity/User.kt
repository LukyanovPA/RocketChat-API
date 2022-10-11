package com.pavellukyanov.domain.auth.entity

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId val id: ObjectId = ObjectId(),
    val username: String,
    val password: String,
    val email: String,
    val avatar: String?
)
