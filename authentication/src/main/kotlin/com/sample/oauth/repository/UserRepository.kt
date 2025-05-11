package com.sample.oauth.repository

import com.sample.oauth.db.entity.User
import com.sample.oauth.db.mapper.UserMapper
import com.sample.oauth.model.UserRole
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    val userMapper: UserMapper
) {
    fun findByEmail(email: String): User? =
        userMapper.findByEmail(email = email)

    fun register(email: String, password: String) {
        userMapper.insertUser(email, password, UserRole.USER.name)
    }
}