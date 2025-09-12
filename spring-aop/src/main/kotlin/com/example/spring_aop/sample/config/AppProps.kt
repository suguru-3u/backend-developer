package com.example.spring_aop.sample.config

import org.apache.logging.log4j.util.Strings
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("sample")
class AppProps(
    val stringName: Strings
)