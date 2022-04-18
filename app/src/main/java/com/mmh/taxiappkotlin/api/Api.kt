package com.mmh.taxiappkotlin.api

import com.mmh.taxiappkotlin.entities.*
import retrofit2.Call
import retrofit2.http.*

interface Api {


    @POST("users")
    fun createUser(@Body user: User): Call<ServerResponse>

    @GET("login")
    fun signIn(
        @Query("username") userName: String,
        @Query("password") password: String
    ): Call<SignInResponse>

    @POST("classes/Order")
    fun createOrder(@Body order: Order): Call<ServerResponse>

    @GET("classes/Order")
    fun getOrders(): Call<GetOrderResponse>

    @PUT("classes/Order/{objectId}")
    fun updateOrder(
        @Path("objectId") objectId: String,
        @Body order: Order
    ): Call<ServerResponse>


}