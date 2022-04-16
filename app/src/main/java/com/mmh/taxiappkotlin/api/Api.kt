package com.mmh.taxiappkotlin.api

import com.mmh.taxiappkotlin.entities.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {


    @POST("users")
    fun createUser(@Body user: User): Call<CreateUserResponse>

    @GET("login")
    fun signIn(@Query ("username") userName:String,
               @Query("password") password: String): Call<SignInResponse>

    @POST("classes/Order")
    fun createOrder(@Body order: Order): Call<CreateUserResponse>

    @GET("classes/Order")
    fun  getOrders(): Call<GetOrderResponse>


}