package com.sample.oauth.db.mapper

import com.sample.oauth.db.entity.User
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.jdbc.SQL

@Mapper
interface UserMapper {
    @SelectProvider(type = UserSqlProvider::class, method = "findUser")
    fun findByEmail(@Param("email") email: String): User?

    @InsertProvider(type = UserSqlProvider::class, method = "insertUser")
    fun insertUser(
        @Param("email") email: String,
        @Param("password") password: String,
        @Param("role") role: String
    )
}

internal class UserSqlProvider {
    fun insertUser(
        @Param("email") email: String,
        @Param("password") password: String,
        @Param("role") role: String
    ): String = SQL().apply {
        INSERT_INTO("users")
        VALUES("email", "#{email}")
        VALUES("password", "#{password}")
        VALUES("role", "#{role}")
    }.toString()

    fun findUser(@Param("email") email: String): String = SQL().apply {
        SELECT(
            "email",
            "password",
            "role",
        )
        FROM("users")
        WHERE("email = #{email}")
    }.toString()
}


