package com.fit2081.app.DatabaseSetup.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fit2081.app.DatabaseSetup.entities.Patient
import kotlinx.coroutines.flow.Flow


@Dao
interface PatientDao {
    // Bulk insert patients from the CSV
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    // Get a specific patients details (can be used later for displaying on profile)
    @Query("SELECT * FROM Patients WHERE userId = :userId")
    fun getPatient(userId: String): LiveData<Patient?>

    // get all patients
    @Query("SELECT * FROM patients ORDER BY userId ASC")
    fun getAllPatients(): Flow<List<Patient>>

    // check patients login details
    @Query("SELECT * FROM patients WHERE userId = :userId AND password = :password")
    suspend fun verifyPatientCredentials(userId: String, password: String): Patient?

    @Update
    suspend fun updatePatient(patient: Patient)

    //to get fruit score
    @Query("SELECT fruits FROM Patients WHERE userId = :userId")
    suspend fun getFruitScore(userId: String): String

}
