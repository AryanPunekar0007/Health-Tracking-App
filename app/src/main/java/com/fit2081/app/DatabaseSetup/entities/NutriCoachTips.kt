package com.fit2081.app.DatabaseSetup.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutri_coach_tips")
data class NutriCoachTips(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val prompt: String,
    val response: String,
    val date: Long
)