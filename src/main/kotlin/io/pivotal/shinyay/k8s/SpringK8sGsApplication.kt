package io.pivotal.shinyay.k8s

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringK8sGsApplication

fun main(args: Array<String>) {
	runApplication<SpringK8sGsApplication>(*args)
}
