package com.l9rins.trademate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val address: String? = null
)

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val clientName: String,
    val price: Double,
    val status: String,
    val date: String,
    val photoUri: String? = null // NEW FIELD
)