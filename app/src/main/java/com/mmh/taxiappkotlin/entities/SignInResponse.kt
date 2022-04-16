package com.mmh.taxiappkotlin.entities

data class SignInResponse(
    val createdAt: String,
    val email: String,
    val objectId: String,
    val phone: String,
    val sessionToken: String,
    val updatedAt: String,
    val userType: String,
    val username: String
)