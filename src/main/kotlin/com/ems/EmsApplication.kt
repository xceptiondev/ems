package com.ems

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@MapperScan("com.ems.mapper")
@SpringBootApplication
@Push
class EmsApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<EmsApplication>(*args)
}
