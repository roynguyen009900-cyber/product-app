package com.example.productapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ProductAppApplication

fun main(args: Array<String>) {
    runApplication<ProductAppApplication>(*args)
}
