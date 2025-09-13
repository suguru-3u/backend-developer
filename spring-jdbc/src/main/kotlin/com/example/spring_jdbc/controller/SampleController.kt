package com.example.spring_jdbc.controller

import com.example.spring_jdbc.domain.User
import com.example.spring_jdbc.repository.SampleRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController(
    private val sampleRepository: SampleRepository
) {
    @PostMapping("/save")
    fun save() {
        sampleRepository.save(
            User(
                name = "sampleName",
                email = "sampleEmail"
            )
        )
    }

    @GetMapping("/findAll")
    fun findAll(): List<User> {
        return sampleRepository.findAll2()
    }

    @GetMapping("find/{id}")
    fun find(@PathVariable id: Int): MutableMap<String, Any> {
        val result =  sampleRepository.find2(id)
        println(result)
        return result
    }
}