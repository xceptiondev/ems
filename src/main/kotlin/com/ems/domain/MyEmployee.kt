package com.ems.domain

import java.time.LocalDate

// src/main/java/com/ems/domain/Employee.kt
data class MyEmployee(
    var id: Long? = null,
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var position: String = "",
    var department: String = "",
    var salary: Double = 0.0,
    var hireDate: LocalDate = LocalDate.now(),
    var phoneNumber: String? = null,
    val address: String? = null
)