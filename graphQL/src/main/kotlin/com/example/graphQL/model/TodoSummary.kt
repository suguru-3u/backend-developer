package com.example.graphQL.model

data class TodoSummary(
    val count: Int,
    val todos: List<Todo>
)