package com.ems.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "employees")
data class Employee(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var firstName: String = "",

    @Column(nullable = false)
    var lastName: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var position: String = "",

    @Column(nullable = false)
    var department: String = "",

    @Column(nullable = false)
    var hireDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var salary: Double = 0.0,

    @Column(nullable = true)
    var phoneNumber: String? = null,

    @Column(nullable = true)
    var address: String? = null
)