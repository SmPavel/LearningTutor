package com.learningtutor.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "repetitionschedule")
data class RepetitionSchedule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val topic: String,
    var nextReviewDate: Date,
    var easeFactor: Double = 2.5,
    var intervalDays: Int = 1,
    var repetitionCount: Int = 0,
    val subject: String = "Математика"
)