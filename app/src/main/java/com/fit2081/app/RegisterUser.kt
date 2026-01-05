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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.app.DatabaseSetup.viewmodels.PatientVM
import com.fit2081.app.ui.theme.AppTheme
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType


class RegisterUser : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get data passed from login screen
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""

        setContent {
            AppTheme {
                RegistrationScreen(
                    context = this@RegisterUser,
                    viewModel = viewModel(),
                    modifier = Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    context: Context,
    viewModel: PatientVM,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") } // Added name state

    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    val patients by viewModel.patientRepository.getAllPatients.collectAsState(initial = emptyList())

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
        // User ID Dropdown
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
                                name = patient.name // Set name when user is selected
                                expanded = false
                                phoneNumber = ""
                                password = ""
                                confirmPassword = ""
                                phoneNumberError = false
                                passwordError = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name Field - Added this new field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = false // Make it read-only since it's populated from the selected user
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                phoneNumberError = false
            },
            label = { Text("Phone Number") },
            isError = phoneNumberError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = false
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (phoneNumberError) {
            Text(
                text = "Phone number does not match records.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        if (passwordError) {
            Text(
                text = "Passwords do not match or are empty.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = {
                val matchedPatient = selectedPatient

                if (matchedPatient == null || phoneNumber != matchedPatient.phoneNumber) {
                    phoneNumberError = true
                } else if (password.isBlank() || password != confirmPassword) {
                    passwordError = true
                } else {
                    // Update patient with new password
                    val updated = matchedPatient.copy(password = password, name = name)
                    viewModel.updatePatient(updated)

                    Toast.makeText(context, "Account registered successfully!", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, FoodIntakeQuestionnaire::class.java).apply{
                        putExtra("USER_ID", selectedUserId)
                        putExtra("NAME", name)
                    })
                    (context as? Activity)?.finish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register Account")
        }
    }
}


//1, 61436567331, Hello@123

