package com.fit2081.app.DatabaseSetup.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "FoodIntake",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["userId"],
            childColumns = ["patientId"],
            onUpdate = ForeignKey.CASCADE
        )
    ],
)

data class FoodIntake(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: String,  // Foreign key

    // Food preferences
    val fruits: Boolean,
    val vegetables: Boolean,
    val grains: Boolean,
    val redMeat: Boolean,
    val seafood: Boolean,
    val poultry: Boolean,
    val fish: Boolean,
    val eggs: Boolean,
    val nutsSeeds: Boolean,

    // Persona
    val persona: String,

    // Timings
    val mealTime: String,
    val sleepTime: String,
    val wakeTime: String,
)