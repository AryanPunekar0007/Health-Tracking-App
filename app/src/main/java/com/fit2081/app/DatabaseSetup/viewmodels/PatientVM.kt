package com.fit2081.app.DatabaseSetup.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fit2081.app.DatabaseSetup.AppDatabase
import com.fit2081.app.DatabaseSetup.Repos.PatientRepo
import com.fit2081.app.DatabaseSetup.entities.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PatientVM(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).patientDao()
    val patientRepository = PatientRepo(dao)
    private val _fruitScore = MutableLiveData<Int>()    //used mutable since only VM can modify it
    val fruitScore: LiveData<Int> = _fruitScore         // livedata since its public

    // Update patient
    fun updatePatient(patient: Patient) = viewModelScope.launch {
        patientRepository.updatePatient(patient)
    }

    // LiveData for observing patient data
    val patient: LiveData<Patient?> get() = patientRepository.getPatient("")

    // Verify credentials (returns result via callback)
    suspend fun verifyPatientCredentials(userId: String, password: String): Patient? {
        return patientRepository.verifyPatientCredentials(userId, password)
    }

    // Called from Activity, loads data from csv into database
    fun loadData(context: Context) = viewModelScope.launch {
        patientRepository.loadPatientsFromCsv(getApplication())
    }

    // get id
    fun getPatientById(userId: String): LiveData<Patient?> {
        return patientRepository.getPatient(userId)
    }

    // get fruit score of a patient
    fun loadFruitScore(userId: String) {
        viewModelScope.launch {
            val scoreStr = patientRepository.getFruitScore(userId)
            val score = scoreStr.toIntOrNull() ?: 0
            _fruitScore.value = score
        }
    }


    val allPatients: Flow<List<Patient>> = patientRepository.getAllPatients // Auto updates on DB changes with flow



}