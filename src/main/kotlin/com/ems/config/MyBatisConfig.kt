package com.ems.config

import com.ems.mapper.EmployeeMapper
import javax.sql.DataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// src/main/java/com/ems/config/MyBatisConfig.kt
@Configuration
class MyBatisConfig {
    @Bean
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        return SqlSessionFactoryBuilder()
            .build(
                org.apache.ibatis.session.Configuration().apply {
                    addMapper(EmployeeMapper::class.java)
                    environment = Environment(
                        "default",
                        JdbcTransactionFactory(),
                        dataSource
                    )
                }
            )
    }
}