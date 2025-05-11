package com.sample.oauth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class InputUserAuthenticationInfo(
    @JsonProperty("email")
    val email: String,
    @JsonProperty("password")
    val password: String
)