package com.fit2081.app.DatabaseSetup.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fit2081.app.DatabaseSetup.entities.FoodIntake

@Dao
interface FoodIntakeDao {
    //insert into table
    @Insert
    suspend fun insert(foodIntake: FoodIntake)

    @Query("SELECT * FROM FoodIntake WHERE patientId = :patientId")
    suspend fun getFoodIntakeByPatientId(patientId: String): FoodIntake?
}