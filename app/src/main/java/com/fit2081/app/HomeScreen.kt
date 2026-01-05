package com.fit2081.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.app.DatabaseSetup.Repos.NutriCoachTipsRepo
import com.fit2081.app.DatabaseSetup.entities.NutriCoachTips
import com.fit2081.app.DatabaseSetup.viewmodels.PatientVM
import com.fit2081.app.DatabaseSetup.viewmodels.FruitVM
import com.fit2081.app.DatabaseSetup.viewmodels.GenAIVM
import com.fit2081.app.ui.theme.AppTheme
import com.fit2081.app.ui.theme.UiState
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage


class HomeScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController: NavHostController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MyBottomBar(navController) }
                ) { innerPadding ->
                    MyNavHost(navController, innerPadding)
                }
            }
        }
    }
}


@Composable
fun EditQuestionnaire(
    userId: String,  // Should be passed from parent composable or navigation
    patientViewModel: PatientVM = viewModel()
) {
    val context = LocalContext.current

    // Fetch patient data from database
    val patient by patientViewModel.getPatientById(userId).observeAsState(initial = null)

    // Show loading state while patient data is being fetched
    if (patient == null) {
        CircularProgressIndicator()
        return
    }

    Button(
        onClick = {
            Toast.makeText(context, "Editing Preferences!", Toast.LENGTH_SHORT).show()
            context.startActivity(
                Intent(context, FoodIntakeQuestionnaire::class.java).apply {  // Launch FoodIntakeQuestionnaire activity with patient data
                    putExtra("USER_ID", userId)
                    putExtra("NAME", patient?.name ?: "")
                    putExtra("EDIT_MODE", true)
                }
            )
        },
        modifier = Modifier.padding(16.dp) //need for around the button padding
    ) {
        Text("Edit Preferences")
    }
}

@Composable
fun MyBottomBar(navController: NavHostController) {

    var selectedItem by remember { mutableStateOf(0) }      // State to track currently selected item in the bottom navigation bar
    val items = listOf(
        "Home",
        "Insights",
        "NutriCoach",
        "Settings"
    )

    NavigationBar {
        // Iterate through each item in items list along with its index
        items.forEachIndexed { index, item ->
            // NavigationBarItem for each item in the list.
            NavigationBarItem(
                // define icon based on the items name.
                icon = {
                    when (item) {
                        "Home" -> Icon(Icons.Filled.Home, contentDescription = "Home")

                        "Insights" -> Icon(Icons.Filled.Face, contentDescription = "Reports")

                        "NutriCoach" -> Icon(Icons.Filled.Person, contentDescription = "Reports")

                        "Settings" -> Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                // display items name as label
                label = { Text(item) },

                selected = selectedItem == index,

                onClick = {
                    // update selectedItem state to the current index.
                    selectedItem = index
                    navController.navigate(item)
                }
            )
        }
    }
}

@Composable
fun MyNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    val context = LocalContext.current

    // Get user ID from activity intent
    val userId = remember {
        (context as? Activity)?.intent?.getStringExtra("USER_ID") ?: ""
    }



    NavHost(
        navController = navController,
        startDestination = "Home"
    ) {
        composable("Home") {
            HomesScreen(navController, userId)
        }

        composable("Insights") {
            InsightsScreen(navController,userId = userId)
        }

        composable("NutriCoach") {
            val tipsRepository = remember { NutriCoachTipsRepo(context) }
            val patientViewModel: PatientVM = viewModel()
            val fruitViewModel: FruitVM = viewModel()
            val genAiViewModel: GenAIVM = viewModel(factory = GenAIVM.GenAIViewModelFactory(tipsRepository) )  // Pass the factory

            NutriCoachScreen(patientViewModel = patientViewModel, fruitViewModel = fruitViewModel, genAiViewModel = genAiViewModel, userId = userId)
        }

        composable("Settings") {
            SettingsScreen(userId)
        }
    }
}

@Composable
fun HomesScreen(navController: NavHostController, userId: String) {
    val context = LocalContext.current
    val patientViewModel: PatientVM = viewModel()



    // Fetch patient data from database
    val patient by patientViewModel.getPatientById(userId).observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User greeting section
        Column(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 60.dp, start = 16.dp)
        ) {
            Text(
                text = "Hello,",
                fontSize = 20.sp,
                color = Color.Gray
            )

            Text(
                text = patient?.name ?: "NAME",
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You've already filled in your Food Intake Questionnaire, but you can change details by clicking on Edit:",
                        fontSize = 10.sp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    EditQuestionnaire(userId)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.balanced_nutrition),
            contentDescription = "Food Plate",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Score",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "See all scores",
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            navController.navigate("Insights")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HEIFA Score display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_up_circle_icon_256x256_20qgouqv),
                    contentDescription = "Up arrow",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Your Food Quality Score",
                    fontSize = 14.sp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${patient?.heifaScore ?: "0"}/100",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()

        Text(
            text = "What is the Food Quality Score?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 1.dp)
        )

        Text(
            text = "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.",
            fontSize = 10.sp
        )

        Text(
            text = "This personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.",
            fontSize = 10.sp
        )
    }
}


@Composable
fun InsightsScreen(navController: NavHostController,userId: String) {
    val context = LocalContext.current
    val patientViewModel: PatientVM = viewModel()
    val patient by patientViewModel.getPatientById(userId).observeAsState()


    // List of food categories with their maximum recommended values
    val foodCategories = listOf(
        "vegetables" to 10,
        "fruits" to 10,
        "grainsAndCereals" to 5,
        "meatAlt" to 10,
        "dairyAlt" to 10,
        "water" to 10,
        "saturatedFat" to 5,
        "unsaturatedFat" to 5,
        "sodium" to 100,
        "addedSugars" to 10,
        "alcohol" to 5,
        "discretionaryFood" to 10
    )

    // Convert patient data to category values
    val foodCategoryValues = remember(patient) {
        foodCategories.map { (category, maxValue) ->
            val value = when (category) {
                "vegetables" -> patient?.vegetables?.toFloatOrNull() ?: 0f
                "fruits" -> patient?.fruits?.toFloatOrNull() ?: 0f
                "grainsAndCereals" -> patient?.grainsAndCereals?.toFloatOrNull() ?: 0f
                "meatAlt" -> patient?.meatAlt?.toFloatOrNull() ?: 0f
                "dairyAlt" -> patient?.dairyAlt?.toFloatOrNull() ?: 0f
                "water" -> patient?.water?.toFloatOrNull() ?: 0f
                "saturatedFat" -> patient?.saturatedFat?.toFloatOrNull() ?: 0f
                "unsaturatedFat" -> patient?.unsaturatedFat?.toFloatOrNull() ?: 0f
                "sodium" -> patient?.sodium?.toFloatOrNull() ?: 0f
                "addedSugars" -> patient?.addedSugars?.toFloatOrNull() ?: 0f
                "alcohol" -> patient?.alcohol?.toFloatOrNull() ?: 0f
                "discretionaryFood" -> patient?.discretionaryFood?.toFloatOrNull() ?: 0f
                else -> 0f
            }
            Triple(category, value, maxValue)
        }
    }


    // AI help for showing heifa scores
    // I used ChatGPT (https://chat.openai.com/) to understand Triple() in order to list the heifa scores
    // The tool was used to provide insights on how to use it and in what context


    val heifaScoreValue = remember(patient) {
        val value = patient?.heifaScore?.toFloatOrNull() ?: 0f
        Triple("heifaScore", value, 100)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Insights",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Text(
            text = "Total Food Quality Score",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val (category, value, maxValue) = heifaScoreValue  // de-structure heifa score triple

        Text(
            text = "${value.toInt()}/$maxValue",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
        ) {
            val progress = (value / maxValue).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color.Green, shape = RoundedCornerShape(50))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foodCategoryValues) { (category, value, maxValue) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )

                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(10.dp)
                            .background(
                                Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        val progress = (value / maxValue).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width((progress * 80).dp)
                                .background(Color.Green, shape = RoundedCornerShape(50))
                        )
                    }

                    Text(
                        text = "${value.toInt()}/$maxValue",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val shareText = buildString {
                    append("My Health Insights:\n\n")
                    (foodCategoryValues + heifaScoreValue).forEach { (category, value, maxValue) ->
                        append("${category.replaceFirstChar { it.uppercase() }}: ${value.toInt()}/$maxValue\n")
                    }
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share insights via"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share with someone")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {navController.navigate("nutriCoach")},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Improve my Diet")
        }
    }
}


@Composable
fun NutriCoachScreen(
    patientViewModel: PatientVM,
    fruitViewModel: FruitVM,
    genAiViewModel: GenAIVM,
    userId: String
) {
    val fruitScore by patientViewModel.fruitScore.observeAsState()
    var randomImageUrl by remember { mutableStateOf("https://picsum.photos/600/300") }


    // AI help for showing a picture
    // I used ChatGPT (https://chat.openai.com/) to understand LaunchedEffect() to display image
    // The tool was used to provide insights on how to use it and in what context

    // Load the score and refresh image when needed
    LaunchedEffect(Unit) {
        patientViewModel.loadFruitScore(userId)
        // Generate a new random image URL
        randomImageUrl = "https://picsum.photos/600/300?random=${System.currentTimeMillis()}"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        fruitScore?.let { score ->
            when {
                score <= 2 -> {
                    FruitInfoSection(viewModel = fruitViewModel, isFruitScoreLow = true)
                }

                else -> {
                    // Show random image if score is good
                    AsyncImage(
                        model = randomImageUrl,
                        contentDescription = "Healthy food inspiration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Great job on your fruit intake!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Keep up the healthy habits!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Always show GenAI section
        Spacer(modifier = Modifier.height(24.dp))
        GenAITipSection(
            viewModel = genAiViewModel,
            userId = userId,
            defaultPrompt = "Generate a short encouraging message about healthy eating"
        )
    }
}

@Composable
fun GenAITipSection(
    viewModel: GenAIVM,
    userId: String,
    defaultPrompt: String
) {
    var promptText by remember { mutableStateOf(defaultPrompt) }
    var showTipsDialog by remember { mutableStateOf(false) } // Changed to control dialog visibility
    val uiState by viewModel.uiState.collectAsState()
    val savedTips by viewModel.savedTips.collectAsState(initial = emptyList())

    // Load saved tips when first shown
    LaunchedEffect(Unit) {
        viewModel.loadSavedTips(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Motivational Tips",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            label = { Text("Request a motivational tip") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.sendPrompt(promptText) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate Tip")
            }

            Button(
                onClick = { showTipsDialog = true }, // Show dialog when clicked
                modifier = Modifier.weight(1f)
            ) {
                Text("Show All Tips")
            }
        }

        // State handling for generating new tips
        when (uiState) {
            is UiState.Success -> {
                val tip = (uiState as UiState.Success).outputText
                Text(text = tip,
                    modifier = Modifier.padding(bottom = 1.dp))
                Button(
                    onClick = {
                        viewModel.saveTip(
                            userId = userId,
                            prompt = promptText,
                            tip = tip
                        )
                    }
                ) {
                    Text("Save this tip")
                }
            }
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> {
                Text(
                    text = (uiState as UiState.Error).errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
            UiState.Initial -> {} // Empty initial state
        }

        // Dialog for showing all tips
        if (showTipsDialog) {
            AlertDialog(
                onDismissRequest = { showTipsDialog = false },
                title = { Text("Your Saved Tips") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (savedTips.isEmpty()) {
                            Text("You haven't saved any tips yet")
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                items(savedTips) { tip ->
                                    SavedTipItem(tip = tip)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showTipsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

// Simplified SavedTipItem
@Composable
fun SavedTipItem(tip: NutriCoachTips) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = tip.prompt,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = tip.response,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = SimpleDateFormat("MMM dd, yyyy").format(Date(tip.date)),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun FruitInfoSection(viewModel: FruitVM, isFruitScoreLow: Boolean) {
    var fruitName by remember { mutableStateOf("") }

    if (isFruitScoreLow) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Your fruit intake is low. Learn about fruits!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fruitName,
                    onValueChange = { fruitName = it },
                    label = { Text("Enter a fruit name") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.fetchFruitInfo(fruitName) },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Search")
                }
            }

            val fruitData by viewModel.fruitData.observeAsState()
            val error by viewModel.error.observeAsState()

            fruitData?.let { fruit ->
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    // Basic info
                    CompactInfoBox(label = "Family", value = fruit.family)
                    CompactInfoBox(label = "Genus", value = fruit.genus)
                    CompactInfoBox(label = "Order", value = fruit.order)

                    // Nutrition info
                    CompactInfoBox(label = "Calories", value = fruit.nutritions.calories.toString())
                    CompactInfoBox(label = "Protein", value = fruit.nutritions.protein.toString())
                    CompactInfoBox(label = "Fat", value = fruit.nutritions.fat.toString())
                    CompactInfoBox(label = "Sugar", value = fruit.nutritions.sugar.toString())
                    CompactInfoBox(label = "Carbohydrates", value = fruit.nutritions.carbohydrates.toString())
                }
            }

            error?.let {
                Text(
                    text = "Error: Try again!",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CompactInfoBox(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}




@Composable
fun ClinicianLoginScreen(
    onSuccess: () -> Unit  // Callback when login succeeds
) {
    var clinicianKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clinician Login",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Clinician Key",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = clinicianKey,
            onValueChange = {
                clinicianKey = it
                showError = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Enter your clinician key") },
            isError = showError,
            singleLine = true
        )

        if (showError) {
            Text(
                text = "Invalid clinician key",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (clinicianKey == "dollar-entry-apples") {
                    onSuccess()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}



@Composable
fun ClinicianDashboard(
    patientViewModel: PatientVM,
    onBack: () -> Unit
) {
    // Collect the Flow as State
    val allPatients by patientViewModel.allPatients.collectAsState(initial = emptyList())

    // AI help for showing average scores
    // I used ChatGPT (https://chat.openai.com/) to understand .filter()
    // The tool was used to provide insights on how to use it and in what context

    val (maleAvg, femaleAvg) = remember(allPatients) {
        val males = allPatients.filter { it.sex.equals("male", ignoreCase = true) }
        val females = allPatients.filter { it.sex.equals("female", ignoreCase = true) }

        val maleAvg = if (males.isNotEmpty()) {
            males.mapNotNull { it.heifaScore.toDoubleOrNull() }.average()
        } else 0.0

        val femaleAvg = if (females.isNotEmpty()) {
            females.mapNotNull { it.heifaScore.toDoubleOrNull() }.average()
        } else 0.0

        maleAvg to femaleAvg
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clinician Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to settings")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Male Patients",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Average HeiFA Score: ${"%.2f".format(maleAvg)}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Female Patients",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Average HeiFA Score: ${"%.2f".format(femaleAvg)}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}



@Composable
fun SettingsScreen(userId: String) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf(0) } // 0 = settings, 1 = login, 2 = dashboard
    val patientViewModel: PatientVM = viewModel()
    val patient by patientViewModel.getPatientById(userId).observeAsState()

    when (screenState) {
        0 -> { // Main settings screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // Title with more spacing
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // ACCOUNT SECTION
                Text(
                    text = "ACCOUNT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // User info with increased spacing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = patient?.name ?: "NAME",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = patient?.phoneNumber ?: "No phone number",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "ID: $userId",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Divider with more space
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // OTHER SETTINGS SECTION
                Text(
                    text = "OTHER SETTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logout Button
                    Button(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                            (context as Activity).finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Clinician Login Button
                    Button(
                        onClick = { screenState = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "Clinician Login",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        1 -> { // Login screen
            ClinicianLoginScreen(
                onSuccess = { screenState = 2 } // Show dashboard on success
            )
        }
        2 -> { // Dashboard screen
            ClinicianDashboard(
                patientViewModel = patientViewModel,
                onBack = { screenState = 0 } // Go back to settings
            )
        }
    }
}

