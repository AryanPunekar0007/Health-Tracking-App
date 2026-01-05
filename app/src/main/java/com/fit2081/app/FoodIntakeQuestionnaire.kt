package com.fit2081.app

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.fit2081.app.DatabaseSetup.AppDatabase
import com.fit2081.app.DatabaseSetup.DAOs.FoodIntakeDao
import com.fit2081.app.DatabaseSetup.entities.FoodIntake
import com.fit2081.app.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodIntakeQuestionnaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get database and DAO instances
        val database = AppDatabase.getDatabase(this)
        val foodIntakeDao = database.foodIntakeDao()

        // Get user ID from intent - finish activity if not provided
        val userId = intent.getStringExtra("USER_ID") ?: run {
            return
        }
        val name = intent.getStringExtra("NAME") ?: run {
            return
        }

        // Check if edit mode on (default to false if not specified)
        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        lifecycleScope.launch {
            // Check for existing food intake record (run on IO thread)
            val existingIntake = withContext(Dispatchers.IO) {
                foodIntakeDao.getFoodIntakeByPatientId(userId)
            }

            if (existingIntake != null && !isEditMode) {
                // Only redirect if not in edit mode
                startActivity(Intent(this@FoodIntakeQuestionnaire, HomeScreen::class.java).apply {
                    putExtra("USER_ID", userId)
                    putExtra("NAME", name)
                })
                finish()
            } else {
                setContent {
                    AppTheme {
                        FoodIntakeQuestionnaireScreen(
                            userId = userId,
                            foodIntakeDao = foodIntakeDao,
                            name = name,
                            isEditMode = isEditMode // Pass this to your screen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodIntakeQuestionnaireScreen(
    userId: String,
    foodIntakeDao: FoodIntakeDao,
    name: String,
    isEditMode: Boolean = false
) {
    // State variables for checkboxes
    val checked1 = remember { mutableStateOf(false) }
    val checked2 = remember { mutableStateOf(false) }
    val checked3 = remember { mutableStateOf(false) }
    val checked4 = remember { mutableStateOf(false) }
    val checked5 = remember { mutableStateOf(false) }
    val checked6 = remember { mutableStateOf(false) }
    val checked7 = remember { mutableStateOf(false) }
    val checked8 = remember { mutableStateOf(false) }
    val checked9 = remember { mutableStateOf(false) }

    // State variables for dropdown
    val dropdownExpanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf("Select an option") }

    // State variables for times
    val mealtime = remember { mutableStateOf("") }
    val sleeptime = remember { mutableStateOf("") }
    val waketime = remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Food Intake Questionnaire",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider()

            // Food Categories Section
            Text(
                text = "Tick all the food categories you can eat",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            FoodCheckboxes(
                checked1, checked2, checked3, checked4,
                checked5, checked6, checked7, checked8, checked9
            )

            // Persona Section
            Text(
                text = "Your Persona",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "People can be broadly classified into 6 different types based on their eating preferences.",
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Modals() // Persona information dialogs

            Text(
                text = "Which Persona best fits you?",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            DropDownMenu(dropdownExpanded, selectedOption)

            // Timings Section
            Text(
                text = "Daily Timings",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            TimeSelectionSection(mealtime, sleeptime, waketime)

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            SaveButton(
                checked1, checked2, checked3, checked4, checked5,
                checked6, checked7, checked8, checked9,
                mealtime, sleeptime, waketime,
                selectedOption, userId, foodIntakeDao, name
            )
        }
    }
}

@Composable
fun TimeSelectionSection(
    mealtime: MutableState<String>,
    sleeptime: MutableState<String>,
    waketime: MutableState<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Meal Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Biggest meal time:",
                modifier = Modifier.weight(1f)
            )
            customTimePicker(
                timeState = mealtime,
                label = "Meal",
                otherTimes = listOf(sleeptime.value, waketime.value),
                modifier = Modifier.weight(1f)
            )
        }

        // Sleep Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Bedtime:",
                modifier = Modifier.weight(1f)
            )
            customTimePicker(
                timeState = sleeptime,
                label = "Sleep",
                otherTimes = listOf(mealtime.value, waketime.value),
                modifier = Modifier.weight(1f)
            )
        }

        // Wake Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Wake-up time:",
                modifier = Modifier.weight(1f)
            )
            customTimePicker(
                timeState = waketime,
                label = "Wake",
                otherTimes = listOf(mealtime.value, sleeptime.value),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun FoodCheckboxes(
    checked1: MutableState<Boolean>,
    checked2: MutableState<Boolean>,
    checked3: MutableState<Boolean>,
    checked4: MutableState<Boolean>,
    checked5: MutableState<Boolean>,
    checked6: MutableState<Boolean>,
    checked7: MutableState<Boolean>,
    checked8: MutableState<Boolean>,
    checked9: MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FoodCheckboxItem("Fruits", checked1)
            FoodCheckboxItem("Vegetables", checked2)
            FoodCheckboxItem("Grains", checked3)
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FoodCheckboxItem("Red Meat", checked4)
            FoodCheckboxItem("Seafood", checked5)
            FoodCheckboxItem("Poultry", checked6)
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FoodCheckboxItem("Fish", checked7)
            FoodCheckboxItem("Eggs", checked8)
            FoodCheckboxItem("Nuts/Seeds", checked9)
        }
    }
}

@Composable
fun FoodCheckboxItem(
    label: String,
    checkedState: MutableState<Boolean>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = { checkedState.value = it }
        )
        Text(text = label, fontSize = 14.sp)
    }
}


@Composable
fun Modals() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // First Row with 3 buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var showDialog1 by remember { mutableStateOf(false) }   // initially not visible
            var showDialog2 by remember { mutableStateOf(false) }
            var showDialog3 by remember { mutableStateOf(false) }

            // Button 1 - Health Devotee
            Button(
                onClick = { showDialog1 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text("Health Devotee", fontSize = 12.sp)
            }
            if (showDialog1) {
                AlertDialog(
                    // switch the visibility to false if user dismisses
                    onDismissRequest = { showDialog1 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_1),
                                contentDescription = "Health Devotee",
                                modifier = Modifier.size(120.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Health Devotee",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "\tI’m passionate about healthy eating & health plays a big " +
                                        "part in my life. I use social media to follow active lifestyle " +
                                        "personalities or get new recipes/exercise ideas. I may even " +
                                        "buy superfoods or follow a particular type of diet. I like to " +
                                        "think I am super healthy.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center // centre button
                        ) {
                            Button(onClick = { showDialog1 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }

            // Button 2 - Mindful Eater
            Button(
                onClick = { showDialog2 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text("Mindful Eater", fontSize = 12.sp)
            }
            if (showDialog2) {
                AlertDialog(
                    onDismissRequest = { showDialog2 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_2),
                                contentDescription = "Mindful Eater",
                                modifier = Modifier.size(120.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Mindful Eater",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "\tI’m health-conscious and being healthy and eating healthy is " +
                                        "important to me. Although health means different things to " +
                                        "different people, I make conscious lifestyle decisions about " +
                                        "eating based on what I believe healthy means. I look for new " +
                                        "recipes and healthy eating information on social media.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showDialog2 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }

            // Button 3 - Wellness Striver
            Button(
                onClick = { showDialog3 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text("Wellness Striver", fontSize = 12.sp)
            }
            if (showDialog3) {
                AlertDialog(
                    onDismissRequest = { showDialog3 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_3),
                                contentDescription = "Wellness Striver",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Wellness Striver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "I aspire to be healthy (but struggle sometimes). Healthy " +
                                        "eating is hard work! I’ve tried to improve my diet, but " +
                                        "always find things that make it difficult to stick with " +
                                        "the changes. Sometimes I notice recipe ideas or healthy " +
                                        "eating hacks, and if it seems easy enough, I’ll give it a go.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showDialog3 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }
        }

        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var showDialog4 by remember { mutableStateOf(false) }
            var showDialog5 by remember { mutableStateOf(false) }
            var showDialog6 by remember { mutableStateOf(false) }

            // Button 4 - Balance Seeker
            Button(
                onClick = { showDialog4 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text(
                    "Balance Seeker",
                    fontSize = 12.sp
                )
            }
            if (showDialog4) {
                AlertDialog(
                    onDismissRequest = { showDialog4 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_4),
                                contentDescription = "Balance Seeker",
                                modifier = Modifier.size(120.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Balance Seeker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "I try and live a balanced lifestyle, and I think that all " +
                                        "foods are okay in moderation. I shouldn’t have to feel " +
                                        "guilty about eating a piece of cake now and again. I get " +
                                        "all sorts of inspiration from social media like finding out " +
                                        "about new restaurants, fun recipes and sometimes healthy " +
                                        "eating tips.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showDialog4 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }

            // Button 5 - Health Procrastinator
            Button(
                onClick = { showDialog5 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text(
                    "Health Procrastinator",
                    fontSize = 10.sp
                )
            }
            if (showDialog5) {
                AlertDialog(
                    onDismissRequest = { showDialog5 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_5),
                                contentDescription = "Health Procrastinator",
                                modifier = Modifier.size(120.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Health Procrastinator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "I’m contemplating healthy eating but it’s not a priority " +
                                        "for me right now. I know the basics about what it means to " +
                                        "be healthy, but it doesn’t seem relevant to me right now. I " +
                                        "have taken a few steps to be healthier but I am not " +
                                        "motivated to make it a high priority because I have too" +
                                        "many other things going on in my life.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showDialog5 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }

            // Button 6 - Food Carefree
            Button(
                onClick = { showDialog6 = true },
                modifier = Modifier.weight(1f)
            )
            {
                Text("Food Carefree")
            }
            if (showDialog6) {
                AlertDialog(
                    onDismissRequest = { showDialog6 = false },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.persona_6),
                                contentDescription = "Food Carefree",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Food Carefree",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "I’m not bothered about healthy eating. I don’t really see " +
                                        "the point and I don’t think about it. I don’t really notice " +
                                        "healthy eating tips or recipes and I don’t care what I eat.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showDialog6 = false }) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun DropDownMenu(dropdownExpanded: MutableState<Boolean>,
                 selectedOption: MutableState<String>) {

    // AI help for box config
    // I used ChatGPT (https://chat.openai.com/) to understand Box Configurations
    // The tool was used to provide insights on how to use it and in what context

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { dropdownExpanded.value = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Selected option text
        Text(text = selectedOption.value)

        // Dropdown arrow button
        IconButton(
            onClick = { dropdownExpanded.value = true },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
        }

        // Dropdown menu
        DropdownMenu(
            expanded = dropdownExpanded.value,
            onDismissRequest = { dropdownExpanded.value = false }
        ) {
            DropdownMenuItem(
                text = { Text("Health Devotee") },
                onClick = {
                    selectedOption.value = "Health Devotee"
                    dropdownExpanded.value = false
                },
            )
            DropdownMenuItem(
                text = { Text("Mindful Eater") },
                onClick = {
                    selectedOption.value = "Mindful Eater"
                    dropdownExpanded.value = false
                },
            )
            DropdownMenuItem(
                text = { Text("Wellness Striver") },
                onClick = {
                    selectedOption.value = "Wellness Striver"
                    dropdownExpanded.value = false
                },
            )
            DropdownMenuItem(
                text = { Text("Balance Seeker") },
                onClick = {
                    selectedOption.value = "Balance Seeker"
                    dropdownExpanded.value = false
                },
            )
            DropdownMenuItem(
                text = { Text("Health Procrastinator") },
                onClick = {
                    selectedOption.value = "Health Procrastinator"
                    dropdownExpanded.value = false
                },
            )
            DropdownMenuItem(
                text = { Text("Food Carefree") },
                onClick = {
                    selectedOption.value = "Food Carefree"
                    dropdownExpanded.value = false
                }
            )
        }
    }
}

// FIX FROM A1 (changed to 1 time picker and doesn't let user have the same times)
@Composable
fun customTimePicker(
    timeState: MutableState<String>,
    label: String,
    otherTimes: List<String>,
    modifier: Modifier = Modifier
): TimePickerDialog {
    val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)
    val errorMessage = remember { mutableStateOf<String?>(null) }

    mCalendar.time = Calendar.getInstance().time

    val timePickerDialog = TimePickerDialog(
        mContext,
        { _, hour: Int, minute: Int ->
            val newTime = "$hour:$minute"

            // error check for times to be different times from each other
            if (otherTimes.contains(newTime)) {
                errorMessage.value = "$label time cannot be the same as other times"
            } else {
                timeState.value = newTime
                errorMessage.value = null
            }
        },
        mHour,
        mMinute,
        false
    )

    Column(modifier = modifier) {
        // Button to show the TimePickerDialog
        Button(onClick = { timePickerDialog.show() }) {
            Text(text = if (timeState.value.isEmpty()) "00:00" else timeState.value)
        }

        errorMessage.value?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    return timePickerDialog
}

@Composable
fun SaveButton(
    checked1: MutableState<Boolean>,
    checked2: MutableState<Boolean>,
    checked3: MutableState<Boolean>,
    checked4: MutableState<Boolean>,
    checked5: MutableState<Boolean>,
    checked6: MutableState<Boolean>,
    checked7: MutableState<Boolean>,
    checked8: MutableState<Boolean>,
    checked9: MutableState<Boolean>,
    mealtime: MutableState<String>,
    sleeptime: MutableState<String>,
    waketime: MutableState<String>,
    selectedOption: MutableState<String>,
    userId: String,
    foodIntakeDao: FoodIntakeDao,
    name: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (!hasSelectedAtLeastOneFood(
                        checked1, checked2, checked3, checked4, checked5,
                        checked6, checked7, checked8, checked9
                    )) {
                    Toast.makeText(context, "Please select at least one food category", Toast.LENGTH_SHORT).show()
                } else if (selectedOption.value == "Select an option") {
                    Toast.makeText(context, "Please select a persona", Toast.LENGTH_SHORT).show()
                } else if (mealtime.value.isEmpty() || sleeptime.value.isEmpty() || waketime.value.isEmpty()) {
                    Toast.makeText(context, "Please enter all time fields", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        try {
                            val foodIntake = FoodIntake(
                                patientId = userId,
                                fruits = checked1.value,
                                vegetables = checked2.value,
                                grains = checked3.value,
                                redMeat = checked4.value,
                                seafood = checked5.value,
                                poultry = checked6.value,
                                fish = checked7.value,
                                eggs = checked8.value,
                                nutsSeeds = checked9.value,
                                persona = selectedOption.value,
                                mealTime = mealtime.value,
                                sleepTime = sleeptime.value,
                                wakeTime = waketime.value
                            )

                            withContext(Dispatchers.IO) {
                                foodIntakeDao.insert(foodIntake)
                            }

                            context.startActivity(
                                Intent(context, HomeScreen::class.java).apply {
                                    putExtra("USER_ID", userId)
                                    putExtra("NAME", name)
                                }
                            )
                            (context as Activity).finish()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error saving preferences: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Save Preferences", fontSize = 16.sp)
        }
    }
}

private fun hasSelectedAtLeastOneFood(
    checked1: MutableState<Boolean>,
    checked2: MutableState<Boolean>,
    checked3: MutableState<Boolean>,
    checked4: MutableState<Boolean>,
    checked5: MutableState<Boolean>,
    checked6: MutableState<Boolean>,
    checked7: MutableState<Boolean>,
    checked8: MutableState<Boolean>,
    checked9: MutableState<Boolean>
): Boolean {
    return checked1.value || checked2.value || checked3.value ||
            checked4.value || checked5.value || checked6.value ||
            checked7.value || checked8.value || checked9.value
}