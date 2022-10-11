package com.pavellukyanov.utils

class MemberAlreadyExistsException : Exception(
    "There is already a member with that username in the room"
)