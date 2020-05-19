package com.google.shinyay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringCloudK8sGsApplication

fun main(args: Array<String>) {
	runApplication<SpringCloudK8sGsApplication>(*args)
}
