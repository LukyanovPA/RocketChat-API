package com.pavellukyanov.base

import kotlinx.serialization.Serializable

@Serializable
data class ObjectResponse<D>(
    val success: Boolean,
    val data: D?,
    val error: String?
)
