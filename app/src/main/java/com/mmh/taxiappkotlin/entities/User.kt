package com.mmh.taxiappkotlin.entities

data class User(
    var objectId: String? = null,
    var email: String? = null,
    var username: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var car: String? = null,
    var userType: String? = null
)
