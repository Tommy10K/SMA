package com.example.notifyclient

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.HTTP

interface ApiService {

    @GET("topics")
    suspend fun getTopics(): TopicsResponse

    @GET("subscriptions/{phone}")
    suspend fun getMySubscriptions(@Path("phone") phone: String): SubscriptionsResponse

    @POST("topics/{topic}/subscribe")
    suspend fun subscribe(
        @Path("topic") topicName: String,
        @Body body: SubscribeRequest
    ): MessageResponse

    @HTTP(method = "DELETE", path = "topics/{topic}/subscribe", hasBody = true)
    suspend fun unsubscribe(
        @Path("topic") topicName: String,
        @Body body: SubscribeRequest
    ): MessageResponse
}