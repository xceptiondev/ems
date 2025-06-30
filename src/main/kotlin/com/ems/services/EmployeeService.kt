package com.ems.services

import com.ems.domain.Employee
import com.ems.repositories.EmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(private val repository: EmployeeRepository) {

    suspend fun findAll(): List<Employee> = withContext(Dispatchers.IO) {
        repository.findAll()
    }
    suspend fun isEmailAvailable(email: String, excludeId: Long?): Boolean {
        val employees = findAll()
        return employees.none { employee ->
            employee.email.equals(email, ignoreCase = true) &&
                    (excludeId == null || employee.id != excludeId)
        }
    }
    suspend fun search(query: String): List<Employee> = withContext(Dispatchers.IO) {
        repository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            query, query, query
        )
    }

    @Transactional
    suspend fun save(employee: Employee): Employee = withContext(Dispatchers.IO) {
        repository.save(employee)
    }

    @Transactional
    suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
            true
        } else {
            false
        }
    }
}