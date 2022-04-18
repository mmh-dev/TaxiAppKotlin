package com.mmh.taxiappkotlin.entities

data class ServerResponse(
    val createdAt: String,
    val updatedAt: String,
    val objectId: String,
    val sessionToken: String
)