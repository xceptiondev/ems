package com.ems.mapper

import com.ems.domain.Employee
import org.apache.ibatis.annotations.Mapper

@Mapper
interface EmployeeMapper {
    fun insert(employee: Employee): Int
    fun update(employee: Employee): Int
    fun delete(employeeId: Long): Int
    fun findById(employeeId: Long): Employee?
    fun findAll(): List<Employee>
    fun findByEmail(email: String): Employee?
    fun isEmailAvailable(email: String, excludeId: Long?): Boolean
    fun findByDepartment(department: String): List<Employee>
    fun batchInsert(employees: List<Employee>): Int
}