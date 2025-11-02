package com.example.notifyclient

import com.google.gson.annotations.SerializedName

// For GET /topics
data class TopicsResponse(
    @SerializedName("topics") val topics: List<String>
)

// For GET /subscriptions/<phone>
data class SubscriptionsResponse(
    @SerializedName("phone") val phone: String,
    @SerializedName("subscriptions") val subscriptions: List<String>
)

// For POST and DELETE bodies
data class SubscribeRequest(
    @SerializedName("phone") val phone: String
)

// For POST and DELETE responses
data class MessageResponse(
    @SerializedName("message") val message: String
)