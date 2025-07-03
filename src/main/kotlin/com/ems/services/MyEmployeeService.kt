package com.ems.services

import com.ems.domain.Employee
import com.ems.repositories.MyEmployeeRepository
import org.springframework.stereotype.Service

// src/main/java/com/ems/service/EmployeeService.kt
@Service
class MyEmployeeService(
    private val repository: MyEmployeeRepository
) {
    fun save(employee: Employee): Employee {
        require(repository.isEmailAvailable(employee.email, employee.id)) {
            "Email already registered"
        }
        return repository.save(employee)
    }

    fun delete(id: Long) = repository.delete(id)
    fun find(id: Long): Employee? = repository.findById(id)
    fun findAll(): List<Employee> = repository.findAll()
    open fun isEmailAvailable(email: String, excludedId: Long?): Boolean = repository.isEmailAvailable(email, excludedId)
}