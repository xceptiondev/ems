package com.ems.mapper

import com.ems.domain.MyEmployee
import org.apache.ibatis.annotations.Mapper

@Mapper
interface EmployeeMapper {
    fun insert(employee: MyEmployee): Int
    fun update(employee: MyEmployee): Int
    fun delete(employeeId: Long): Int
    fun findById(employeeId: Long): MyEmployee?
    fun findAll(): List<MyEmployee>
    fun findByEmail(email: String): MyEmployee?
    fun isEmailAvailable(email: String, excludeId: Long?): Boolean
    fun findByDepartment(department: String): List<MyEmployee>
    fun batchInsert(employees: List<MyEmployee>): Int
}