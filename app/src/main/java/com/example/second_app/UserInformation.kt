package com.example.second_app

import java.io.Serializable

data class UserInformation(
    val id: String,
    val rating: Int,
    val username: String,
): Serializable