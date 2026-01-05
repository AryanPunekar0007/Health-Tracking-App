package com.fit2081.app.DatabaseSetup.Repos

import android.content.Context
import com.fit2081.app.DatabaseSetup.DAOs.PatientDao
import com.fit2081.app.DatabaseSetup.entities.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


class PatientRepo(private val patientDao: PatientDao) {

    val getAllPatients = patientDao.getAllPatients()
    fun getPatient(userId: String) = patientDao.getPatient(userId)
    suspend fun verifyPatientCredentials(userId: String, password: String) =
        patientDao.verifyPatientCredentials(userId, password)


    /// update patinet
    suspend fun updatePatient(patient: Patient) {
        patientDao.updatePatient(patient)
    }

    // function to load patiens
    suspend fun loadPatientsFromCsv(context: Context) {
        if (shouldLoadCsv(context)) {  // Check first launch
            val patients = parsePatientCsv(context)
            patientDao.insertAll(patients)
            markCsvLoaded(context)     // Save flag to SharedPreferences
        }
    }

    // SharedPreferences check (prevents reloading)
    private fun shouldLoadCsv(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean("csv_loaded", false)
    }

    // alr logged in before
    private fun markCsvLoaded(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("csv_loaded", true).apply()
    }



    suspend fun getFruitScore(userId: String): String {
        return patientDao.getFruitScore(userId)
    }


    private suspend fun parsePatientCsv(context: Context): List<Patient> =
        withContext(Dispatchers.IO) {
            val patients = mutableListOf<Patient>()
            try {
                context.assets.open("Data.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.useLines { lines ->
                            lines.forEach { line ->
                                val values = line.split(",")
                                if (values.size >= 63) {  // Same CSV structure as your original
                                    val patient = Patient(
                                        userId = values[1].trim(),      // userId (column 1)
                                        phoneNumber = values[0].trim(), // phone (column 0)
                                        password = "",   // Add a default or map from CSV
                                        sex = values[2].trim(),         // sex (column 2)
                                        heifaScore = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[3].trim()
                                        } else {
                                            values[4].trim()
                                        },
                                        discretionaryFood = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[5].trim()
                                        } else {
                                            values[6].trim()
                                        },
                                        vegetables = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[8].trim()
                                        } else {
                                            values[9].trim()
                                        },
                                        fruits = if (values[2].equals("male", ignoreCase = true)) {
                                            values[19].trim()
                                        } else {
                                            values[20].trim()
                                        },
                                        grainsAndCereals = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[29].trim()
                                        } else {
                                            values[30].trim()
                                        },
                                        wholegrains = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[33].trim()
                                        } else {
                                            values[34].trim()
                                        },
                                        meatAlt = if (values[2].equals("male", ignoreCase = true)) {
                                            values[36].trim()
                                        } else {
                                            values[37].trim()
                                        },
                                        dairyAlt = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[40].trim()
                                        } else {
                                            values[41].trim()
                                        },
                                        sodium = if (values[2].equals("male",
                                                ignoreCase = true)) {
                                            values[43].trim()
                                        } else {
                                            values[44].trim()
                                        },
                                        alcohol = if (values[2].equals("male",
                                                ignoreCase = true)) {
                                            values[46].trim()
                                        } else {
                                            values[47].trim()
                                        },
                                        water = if (values[2].equals("male",
                                                ignoreCase = true)) {
                                            values[49].trim()
                                        } else {
                                            values[50].trim()
                                        },
                                        addedSugars = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[54].trim()
                                        } else {
                                            values[55].trim()
                                        },
                                        saturatedFat = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[57].trim()
                                        } else {
                                            values[58].trim()
                                        },
                                        unsaturatedFat = if (values[2].equals(
                                                "male",
                                                ignoreCase = true
                                            )
                                        ) {
                                            values[60].trim()
                                        } else {
                                            values[61].trim()
                                        },
                                    )
                                    patients.add(patient)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            patients
        }
}