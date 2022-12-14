package com.pavellukyanov.utils

object Errors {
    //Auth
    const val ALREADY_EMAIL = "A user with this email already exists"
    const val ALREADY_USERNAME = "A user with this email already exists"
    const val EMPTY_USERNAME_OR_PASSWORD = "Empty field username or password"
    const val INCORRECT_EMAIL = "Incorrect email"
    const val INCORRECT_PASSWORD = "Incorrect password"
    const val BAD_REFRESH_TOKEN = "Bad Refresh Token"
    const val BAD_USER_ID = "Bad userId"

    //ChatRoom
    const val CHAT_NAME_NOT_SPECIFIED = "Chat name not specified"
    const val USER_IS_NOT_CHAT_OWNER = "This user is not the chat owner"
}