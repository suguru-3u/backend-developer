package com.sample.oauth.db.entity

import com.sample.oauth.model.UserRole

data class User(
    val email: String,
    val password: String,
    val role: UserRole
)
