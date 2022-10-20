package com.pavellukyanov.domain

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<D>(
    val success: Boolean,
    val data: D? = null,
    val errorMessage: String? = null
)