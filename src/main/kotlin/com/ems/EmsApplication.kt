package com.ems

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Push
class EmsApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<EmsApplication>(*args)
}
