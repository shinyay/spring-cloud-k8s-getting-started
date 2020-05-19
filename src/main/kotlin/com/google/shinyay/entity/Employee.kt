package com.google.shinyay.entity

import javax.persistence.*

@Entity
data class Employee(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int,
        @Column(nullable = false, unique = false) val name: String,
        @Column(nullable = false, unique = true) val email: String)