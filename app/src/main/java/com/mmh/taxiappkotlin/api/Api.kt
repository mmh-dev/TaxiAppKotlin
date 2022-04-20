package com.mmh.taxiappkotlin.api

import com.google.android.gms.maps.model.LatLng
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

    @GET("json")
    fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String,
        @Query("alternatives") alternatives: Boolean,
        @Query("language") language: String,
        @Query("avoid") avoid: String
    ): Call<MapsResponse>


}