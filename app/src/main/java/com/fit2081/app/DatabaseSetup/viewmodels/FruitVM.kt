package com.fit2081.app.DatabaseSetup.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fit2081.app.DatabaseSetup.Repos.FruitRepo
import com.fit2081.app.FruitData
import com.fit2081.app.ui.theme.APIService
import kotlinx.coroutines.launch

class FruitVM(application: Application) : AndroidViewModel(application) {



    // AI help for fetchFruitInfo
    // I used ChatGPT (https://chat.openai.com/) to understand how to save tips using MutableLiveData
    // The tool was used to provide insights on how to use it and in what context

    private val apiService = APIService.create()
    private val repository = FruitRepo(apiService)

    private val _fruitData = MutableLiveData<FruitData?>()
    val fruitData: LiveData<FruitData?> = _fruitData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // fetches fruit information from the repo
    fun fetchFruitInfo(fruitName: String) {
        viewModelScope.launch {
            val result = repository.getFruitInfo(fruitName)
            result
                .onSuccess { _fruitData.value = it }    // Update fruit data on successful response
                .onFailure { _error.value = it.localizedMessage ?: "Unknown error" }    // error message
        }
    }
}
