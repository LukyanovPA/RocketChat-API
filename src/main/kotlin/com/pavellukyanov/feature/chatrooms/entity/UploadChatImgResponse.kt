package com.pavellukyanov.feature.chatrooms.entity

import kotlinx.serialization.Serializable

@Serializable
data class UploadChatImgResponse(
    val success: Boolean,
    val src: String?,
    val errorMessage: String?
)