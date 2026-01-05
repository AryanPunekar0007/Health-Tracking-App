package com.fit2081.app.DatabaseSetup.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Patients")
data class Patient(
    @PrimaryKey val userId: String,
    val phoneNumber: String,
    val sex: String,
    val discretionaryFood: String,
    val vegetables: String,
    val fruits: String,
    val grainsAndCereals: String,
    val wholegrains: String,
    val meatAlt: String,
    val dairyAlt: String,
    val sodium: String,
    val alcohol: String,
    val water: String,
    val addedSugars: String,
    val saturatedFat: String,
    val unsaturatedFat: String,
    val heifaScore: String,
    var password: String = "", // Will be set during register account
    var name: String = "",     // Will be set during register account
    var isAccountClaimed: Boolean = false
)
