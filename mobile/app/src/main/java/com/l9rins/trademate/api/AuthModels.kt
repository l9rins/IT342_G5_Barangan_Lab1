package com.l9rins.trademate.api

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("message") val message: String?
)