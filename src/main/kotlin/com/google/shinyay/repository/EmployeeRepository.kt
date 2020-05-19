package com.google.shinyay.repository

import com.google.shinyay.entity.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Int>