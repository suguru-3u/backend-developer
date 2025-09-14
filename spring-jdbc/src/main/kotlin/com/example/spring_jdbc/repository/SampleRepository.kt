package com.example.spring_jdbc.repository

import com.example.spring_jdbc.domain.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet

@Repository
class SampleRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val transactionTemplate: TransactionTemplate // configクラスにトランザクションの設定を細かく記述したりすることができるらしい。トランザクションの分離や伝播など。
) {
    fun save(user: User): Int {
        val sql = "INSERT INTO users (name, email) VALUES (?, ?)"
        return jdbcTemplate.update(sql, user.name, user.email)
    }

    // バインド変数を利用して、SQLの条件を動的に変更する
    fun find(id: Int): MutableMap<String, Any> {
        val sql = "SELECT name, email FROM users WHERE id = ?"
        return jdbcTemplate.queryForMap(sql, id)
    }

    // 名前付きバインド変数
    fun find2(id: Int): MutableMap<String, Any> {
        val sql = "SELECT name, email FROM users WHERE id = :id"
        val params = mapOf("id" to id)
        return namedParameterJdbcTemplate.queryForMap(sql, params)
    }


    // ラムダ式を利用してDBから取得した値をクラスに変換している
    fun findAll(): List<User> {
        val sql = "SELECT name, email FROM users"
        return jdbcTemplate.query(sql) { rs, _ ->
            User(
                rs.getString("name"),
                rs.getString("email")
            )
        }
    }

    // 宣言的トランザクション：アノテーションを付与することでトランザクションを実行することができる（クラスに付与することもできる）
    @Transactional
    fun update() {
        val sql = "UPDATE users SET name = ?, email = ? WHERE id = ?"
        val params = mapOf("name" to "updated_name", "email" to "update_email", "id" to 1)
        jdbcTemplate.update(sql, params)
    }

    // 明示的トランザクション：TransactionTemplateを利用してトランザクションを実行することもできる
    fun update2() {
        transactionTemplate.execute {
            val sql = "UPDATE users SET name = ?, email = ? WHERE id = ?"
            val params = mapOf("name" to "updated_name", "email" to "update_email", "id" to 1)
            jdbcTemplate.update(sql, params)
        }
    }

    // 変換処理をクラスで行うこともできる
    fun findAll2(): List<User> {
        val sql = "SELECT name, email FROM users"
        val rowMapper = UserRowMapper()
        return jdbcTemplate.query(sql, rowMapper)
    }
}

// ResultSetExtractorwを使用することで、複数行から一つのクラスを作成することもできる
// RowCallbackHandlerは戻り値を返さないが、データのチェックやファイル出力に適しているものもある
class UserRowMapper : RowMapper<User> {
    override fun mapRow(rs: ResultSet, rowNum: Int): User {
        return User(
            name = rs.getString("name"),
            email = rs.getString("email")
        )
    }
}