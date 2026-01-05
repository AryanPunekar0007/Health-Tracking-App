package com.fit2081.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.app.DatabaseSetup.viewmodels.PatientVM
import com.fit2081.app.ui.theme.AppTheme
import kotlinx.coroutines.launch


class UserLoginScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // hold screen UI
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CSVLoginScreen(
                            context = this@UserLoginScreen, // passing current context
                            viewModel = viewModel(),
                            modifier = Modifier.padding(innerPadding) )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSVLoginScreen(
    context: Context,
    viewModel: PatientVM,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    val patients by viewModel.patientRepository.getAllPatients.collectAsState(initial = emptyList())

    // Find the selected patient for display
    val selectedPatient = remember(selectedUserId, patients) {
        patients.find { it.userId == selectedUserId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Improved Dropdown Implementation
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedPatient?.let {
                    if (it.name.isNotEmpty()) "${it.userId} - ${it.name}" else it.userId
                } ?: "Select User",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select User") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (patients.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No users available") },
                        onClick = { expanded = false }
                    )
                } else {
                    patients.forEach { patient ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (patient.name.isNotEmpty())
                                        "${patient.userId} - ${patient.name}"
                                    else
                                        patient.userId
                                )
                            },
                            onClick = {
                                selectedUserId = patient.userId
                                expanded = false
                                password = ""
                                passwordError = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Password") },
            isError = passwordError,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (passwordError) {
            Text(
                text = "Invalid credentials",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                if (selectedUserId.isEmpty() || password.isBlank()) {   // check if user id is empty or pass blank (FIX FROM A1)
                    passwordError = true
                } else {
                    viewModel.viewModelScope.launch {   // launch a coroutine in viewmodel
                        val patient = viewModel.verifyPatientCredentials(selectedUserId, password)  // check w database
                        if (patient != null) {
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            context.startActivity(
                                Intent(context, FoodIntakeQuestionnaire::class.java).apply{
                                    // pass for later use
                                    putExtra("USER_ID", selectedUserId)
                                    putExtra("NAME", selectedPatient?.name ?: "")
                                }
                            )
                            (context as Activity).finish()
                        } else {
                            passwordError = true // if cred r invalid
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

// Register Button
        Button(
            onClick = {
                Toast.makeText(context, "Let's Register!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, RegisterUser::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Register New User")
        }
    }
}