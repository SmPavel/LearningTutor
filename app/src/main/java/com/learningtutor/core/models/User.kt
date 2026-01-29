package com.learningtutor.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "Ученик",
    var currentTheta: Double = 0.0,
    val createdAt: Date = Date(),
    var thetaHistoryJson: String = "[]" // JSON список пар [timestamp, theta]
)