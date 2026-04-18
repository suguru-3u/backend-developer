package com.example.spring_load_test.controller

import com.example.spring_load_test.service.SampleService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
class SampleController(
    private val sampleService: SampleService
) {

    @GetMapping("/sample")
    fun sample(): Long {
        return sampleService.sample()
    }
}