package com.fit2081.app.DatabaseSetup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fit2081.app.ui.theme.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.fit2081.app.DatabaseSetup.Repos.NutriCoachTipsRepo
import com.fit2081.app.DatabaseSetup.entities.NutriCoachTips
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow


class GenAIVM(private val tipsRepository: NutriCoachTipsRepo) : ViewModel() {


    // CODE FROM WEEK 7 LAB
    /**
     * Mutable state flow to hold the current UI state.
     * Initially set to 'UiState.Initial'.
     */
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)

    /**
     * Publicly exposed immutable state flow for observing the UI state.
     */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Instance of the GenerativeModel used to generate content.
     * The model is initialized with a specific model name and API key.
     */
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyDAIDUcl0QsgfHRsxbZ8IKHjj974RAz_-c"
    )

    /**
     * Sends a prompt to the generative AI model and updates the UI state
     * based on the response.
     *
     * @param prompt The input text prompt to be sent to the generative model.
     */
    fun sendPrompt(prompt: String) {
        // Set the UI state to loading before making the api call.
        _uiState.value = UiState.Loading

        // Launch a coroutine in the IO dispatcher to perform the api call
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Generate content using the generative model
                val response = generativeModel.generateContent(
                    content {
                        text(prompt) // Set the input text for the model.
                    }
                )

                // Update the UI state with the generated content if successful
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                // Update the UI state with an error message if an exception occurs
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }


    // save tip if the user wants to
    fun saveTip(userId: String, prompt: String, tip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tipsRepository.insertTip(
                    NutriCoachTips(
                        userId = userId,
                        prompt = prompt,
                        response = tip,
                        date = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Handle error
            }
        }

    }

    // AI help for savedTips
    // I used ChatGPT (https://chat.openai.com/) to understand how to save tips using MutableStateFow
    // The tool was used to provide insights on how to use it and in what context

    // Private mutable state flow for holding the list of saved tips
    private val _savedTips = MutableStateFlow<List<NutriCoachTips>>(emptyList())

    // Public immutable state flow exposing the saved tips to UI components
    val savedTips: StateFlow<List<NutriCoachTips>> = _savedTips


    //Loads saved tips for a specific user from the repository
    fun loadSavedTips(userId: String) {
        viewModelScope.launch {
            tipsRepository.getTipsByUser(userId).collect { tips ->
                _savedTips.value = tips
            }
        }
    }



    class GenAIViewModelFactory(
        private val tipsRepository: NutriCoachTipsRepo
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenAIVM::class.java)) {
                return GenAIVM(tipsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}