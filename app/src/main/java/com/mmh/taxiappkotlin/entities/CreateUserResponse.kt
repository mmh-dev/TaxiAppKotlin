package com.mmh.taxiappkotlin.entities

data class CreateUserResponse(
    val createdAt: String,
    val objectId: String,
    val sessionToken: String
)