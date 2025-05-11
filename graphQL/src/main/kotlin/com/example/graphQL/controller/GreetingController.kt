package com.example.graphQL.controller

import com.example.graphQL.model.Todo
import com.example.graphQL.model.TodoFilterInput
import com.example.graphQL.model.TodoSummary
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller

@Controller
class GreetingController {
    private val todos = mutableListOf<Todo>()
    private var nextId = 1

    @MutationMapping
    fun addTodo(@Argument title: String, @Argument done: Boolean): Todo {
        val todo = Todo(id = nextId++, title = title, done = done)
        todos.add(todo)
        return todo
    }

    @QueryMapping
    fun allTodos(): List<Todo> = todos

    @QueryMapping
    fun todoSummary(): TodoSummary {
        return TodoSummary(
            count = todos.size,
            todos = todos
        )
    }

    @QueryMapping
    fun todosByDone(@Argument done: Boolean): List<Todo> {
        return todos.filter { it.done == done }
    }

    @QueryMapping
    fun filterTodos(@Argument filter: TodoFilterInput): List<Todo> {
        return todos.filter { todo ->
            val matchesKeyword = filter.keyword?.let { todo.title.contains(it) } ?: true
            val matchesDone = filter.done?.let { todo.done == it } ?: true
            matchesKeyword && matchesDone
        }
    }

    @MutationMapping
    fun updateTodo(
        @Argument id: Int,
        @Argument title: String?,
        @Argument done: Boolean?
    ): Todo? {
        val todo = todos.find { it.id == id } ?: return null
        title?.let { todo.title = it }
        done?.let { todo.done = it }
        return todo
    }

    @MutationMapping
    fun deleteTodo(@Argument id: Int): Todo? {
        val index = todos.indexOfFirst { it.id == id }
        return if (index != -1) todos.removeAt(index) else null
    }


    @QueryMapping
    fun me(): String {
        val auth = SecurityContextHolder.getContext().authentication
        return "Hello, ${(auth.principal as? UserDetails)?.username ?: "anonymous"}"
    }

    @QueryMapping
    fun hello(): String {
        return "こんにちは、GraphQL！"
    }
}