package com.example.graphQL.model

data class TodoFilterInput(
    val keyword: String? = null,
    val done: Boolean? = null
)