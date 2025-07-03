package com.ems.repositories

import com.ems.domain.Employee
import com.ems.mapper.EmployeeMapper
import org.apache.ibatis.session.SqlSession
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// src/main/java/com/ems/repository/EmployeeRepository.kt
@Repository
@Transactional
class MyEmployeeRepository(
    private val sqlSession: SqlSession
) {
    private val mapper: EmployeeMapper
        get() = sqlSession.getMapper(EmployeeMapper::class.java)

    fun save(employee: Employee): Employee {
        if (employee.id == null) {
            mapper.insert(employee)
        } else {
            mapper.update(employee)
        }
        return employee
    }

    fun delete(id: Long) = mapper.delete(id)
    fun findById(id: Long): Employee? = mapper.findById(id)
    fun findAll(): List<Employee> = mapper.findAll()
    fun isEmailAvailable(email: String, excludeId: Long?): Boolean =
        mapper.isEmailAvailable(email, excludeId)
}