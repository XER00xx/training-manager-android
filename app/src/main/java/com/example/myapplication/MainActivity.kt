package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Główny ViewModel PRO-TRACKER 2026.
 * Zarządza bazą ćwiczeń, planami, sesjami i statystykami.
 */
class WorkoutViewModel : ViewModel() {
    var currentTheme by mutableStateOf(AppTheme.DARK)
    var userProfile by mutableStateOf(UserProfile())
    
    val savedPlans = mutableStateListOf<TrainingPlan>()
    val workoutHistory = mutableStateListOf<SavedWorkout>()
    var activeSession by mutableStateOf<ActiveWorkoutSession?>(null)
    
    var elapsedSeconds by mutableLongStateOf(0L)
    private var lastTickTime = 0L

    // Pełna baza ćwiczeń dla każdej partii mięśniowej
    val allExercises = mutableStateListOf(
        // KLATKA
        Exercise("c1", "Wyciskanie na ławce poziomej", R.string.cat_chest, videoResName = "video_chest"),
        Exercise("c2", "Wyciskanie hantli na skosie +", R.string.cat_chest, videoResName = "video_chest"),
        Exercise("c3", "Rozpiętki hantlami", R.string.cat_chest),
        Exercise("c4", "Pompki na poręczach (klatka)", R.string.cat_chest),
        Exercise("c5", "Krzyżowanie linek (brama)", R.string.cat_chest),
        Exercise("c6", "Hammer Chest Press", R.string.cat_chest),
        Exercise("c7", "Wyciskanie hantli poziomo", R.string.cat_chest),
        Exercise("c8", "Wyciskanie na skosie ujemnym", R.string.cat_chest),
        // PLECY
        Exercise("b1", "Podciąganie na drążku (nachwyt)", R.string.cat_back, videoResName = "video_back"),
        Exercise("b2", "Martwy ciąg klasyczny", R.string.cat_back, videoResName = "video_back"),
        Exercise("b3", "Wiosłowanie sztangą nachwytem", R.string.cat_back, videoResName = "video_back"),
        Exercise("b4", "Wiosłowanie hantlem w opadzie", R.string.cat_back),
        Exercise("b5", "Ściąganie drążka do klatki", R.string.cat_back),
        Exercise("b6", "Przyciąganie linek dolnych", R.string.cat_back),
        Exercise("b7", "Wiosłowanie końcem sztangi (T-Bar)", R.string.cat_back),
        Exercise("b8", "Narciarz na wyciągu", R.string.cat_back),
        // NOGI
        Exercise("l1", "Przysiady ze sztangą (High Bar)", R.string.cat_legs, videoResName = "video_legs"),
        Exercise("l2", "Leg Press (Suwnica)", R.string.cat_legs, videoResName = "video_legs"),
        Exercise("l3", "Przysiady bułgarskie", R.string.cat_legs),
        Exercise("l4", "Wyprosty na maszynie", R.string.cat_legs),
        Exercise("l5", "Uginania na dwugłowe leżąc", R.string.cat_legs),
        Exercise("l6", "Hack Squat", R.string.cat_legs),
        Exercise("l7", "Wykroki z hantlami", R.string.cat_legs),
        Exercise("l8", "Martwy ciąg na prostych nogach", R.string.cat_legs),
        // BARKI
        Exercise("s1", "Wyciskanie żołnierskie OHP", R.string.cat_shoulders, videoResName = "video_shoulders"),
        Exercise("s2", "Wyciskanie hantli siedząc", R.string.cat_shoulders),
        Exercise("s3", "Wznosy bokiem hantlami", R.string.cat_shoulders),
        Exercise("s4", "Facepulls", R.string.cat_shoulders),
        Exercise("s5", "Arnoldki", R.string.cat_shoulders),
        Exercise("s6", "Wznosy w opadzie (tył barku)", R.string.cat_shoulders),
        Exercise("s7", "Podciąganie sztangi do brody", R.string.cat_shoulders),
        // BICEPS
        Exercise("bi1", "Uginanie ramion ze sztangą łamaną", R.string.cat_biceps, videoResName = "video_biceps"),
        Exercise("bi2", "Uginanie hantli z supinacją", R.string.cat_biceps),
        Exercise("bi3", "Młotki hantlami", R.string.cat_biceps),
        Exercise("bi4", "Uginanie na modlitewniku", R.string.cat_biceps),
        Exercise("bi5", "Uginanie na wyciągu dolnym", R.string.cat_biceps),
        // TRICEPS
        Exercise("t1", "Francuskie wyciskanie sztangą", R.string.cat_triceps, videoResName = "video_triceps"),
        Exercise("t2", "Prostowanie ramion na wyciągu (lina)", R.string.cat_triceps),
        Exercise("t3", "Dipy na poręczach", R.string.cat_triceps),
        Exercise("t4", "Wyciskanie wąsko", R.string.cat_triceps),
        Exercise("t5", "Wyprosty za głowę hantlem", R.string.cat_triceps),
        // RDZEŃ
        Exercise("co1", "Plank", R.string.cat_core, videoResName = "video_core"),
        Exercise("co2", "Wznosy nóg w zwisie", R.string.cat_core),
        Exercise("co3", "Allahyc", R.string.cat_core),
        Exercise("co4", "Scyzoryki", R.string.cat_core),
        // PRZEDRAMIONA
        Exercise("f1", "Spacer farmera", R.string.cat_forearms),
        Exercise("f2", "Uginanie nadgarstków", R.string.cat_forearms),
        // POŚLADKI
        Exercise("g1", "Hip Thrust", R.string.cat_glutes),
        Exercise("g2", "Rumuński Martwy Ciąg", R.string.cat_glutes),
        Exercise("g3", "Abdukcja na maszynie", R.string.cat_glutes)
    )

    fun addCustomExercise(name: String, muscleGroupRes: Int) {
        allExercises.add(Exercise(UUID.randomUUID().toString(), name, muscleGroupRes, isCustom = true))
    }

    fun startWorkoutFromPlan(plan: TrainingPlan) {
        activeSession = ActiveWorkoutSession(
            planId = plan.id,
            planName = plan.name,
            date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis()),
            initialExercises = plan.exercises.toList()
        )
        elapsedSeconds = 0L
        lastTickTime = System.currentTimeMillis()
    }

    fun finishWorkout() {
        activeSession?.let { session ->
            val durationStr = formatDuration(elapsedSeconds)
            
            // ARCHIWIZACJA: Przechodzimy przez wszystkie ćwiczenia użyte w sesji (nawet te podmienione)
            val historyData = session.sessionHistoryOrder.map { exId ->
                val name = session.exerciseNamesRegistry[exId] ?: "Nieznane ćwiczenie"
                val sets = session.loggedSets[exId]?.map { 
                    ExerciseHistorySet(it.setNumber, it.weight, it.reps) 
                } ?: emptyList()
                
                ExerciseHistoryData(name = name, sets = sets)
            }
            
            var totalVol = 0.0
            session.loggedSets.values.forEach { sets ->
                sets.filter { it.isCompleted }.forEach {
                    val w = it.weight.toDoubleOrNull() ?: 0.0
                    val r = it.reps.toIntOrNull() ?: 0
                    totalVol += (w * r)
                }
            }
            
            workoutHistory.add(SavedWorkout(
                planName = session.planName,
                date = session.date,
                duration = durationStr,
                durationSeconds = elapsedSeconds,
                exercisesData = historyData,
                totalVolume = totalVol
            ))
        }
        activeSession = null
    }

    fun tick() {
        activeSession?.let {
            if (!it.isPaused) {
                val now = System.currentTimeMillis()
                elapsedSeconds += (now - lastTickTime) / 1000
                lastTickTime = now
            } else {
                lastTickTime = System.currentTimeMillis()
            }
        }
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    fun addSetToActive() {
        activeSession?.let { session ->
            val currentEx = session.exercises[session.currentExerciseIndex]
            session.loggedSets[currentEx.exercise.id]?.add(ExerciseSet(session.loggedSets[currentEx.exercise.id]!!.size + 1))
        }
    }

    fun removeSetFromActive() {
        activeSession?.let { session ->
            val currentEx = session.exercises[session.currentExerciseIndex]
            val list = session.loggedSets[currentEx.exercise.id]
            if (list != null && list.size > 1) {
                list.removeAt(list.size - 1)
            }
        }
    }

    fun getDaysSinceJoined(): Long {
        val diff = System.currentTimeMillis() - userProfile.joinDate
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(1)
    }

    fun renamePlan(planId: String, newName: String) {
        val index = savedPlans.indexOfFirst { it.id == planId }
        if (index != -1) {
            savedPlans[index].name = newName
        }
    }

    fun deletePlan(planId: String) {
        savedPlans.removeAll { it.id == planId }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkoutViewModel = viewModel()
            var showSplashScreen by remember { mutableStateOf(true) }

            // Logika tykania zegara w tle
            LaunchedEffect(viewModel.activeSession) {
                while (viewModel.activeSession != null) {
                    viewModel.tick()
                    delay(1000)
                }
            }

            MyApplicationTheme(appTheme = viewModel.currentTheme) {
                if (showSplashScreen) {
                    AnimatedSplashScreen(onFinished = { showSplashScreen = false })
                } else {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

/**
 * Nowoczesny, animowany ekran startowy ze smooth pojawianiem się.
 */
@Composable
fun AnimatedSplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "SplashAlpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.9f,
        animationSpec = tween(durationMillis = 1500),
        label = "SplashScale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Próba załadowania app_logo.png z drawable
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "PRO-TRACKER 2026",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMuscleGroup by remember { mutableStateOf<Int?>(null) }
    var viewingWorkoutDetails by remember { mutableStateOf<SavedWorkout?>(null) }
    
    var showRenameDialog by remember { mutableStateOf<TrainingPlan?>(null) }
    var showAddCustomDialog by remember { mutableStateOf<Int?>(null) }
    var showSwapDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val tabs = listOf("PROFIL", "ĆWICZENIA", "TRENING")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(32.dp))
                Text("MENU PRO-TRACKER", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black)
                
                NavigationDrawerItem(
                    label = { Text("STRONA GŁÓWNA") },
                    selected = selectedTab == 0 && viewingWorkoutDetails == null,
                    onClick = { selectedTab = 0; viewingWorkoutDetails = null; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(16.dp))
                Text("MOJE PLANY", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp, color = Color.Gray)
                
                if (viewModel.savedPlans.isEmpty()) {
                    Text("Brak planów. Dodaj je w Ćwiczeniach!", modifier = Modifier.padding(16.dp), fontSize = 11.sp)
                }
                
                viewModel.savedPlans.forEach { plan ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
                        NavigationDrawerItem(
                            label = { Text(plan.name) },
                            selected = viewModel.activeSession?.planId == plan.id,
                            onClick = { viewModel.startWorkoutFromPlan(plan); selectedTab = 2; viewingWorkoutDetails = null; scope.launch { drawerState.close() } },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showRenameDialog = plan }) {
                            Icon(Icons.Default.Edit, "Zmień nazwę", modifier = Modifier.size(18.dp), tint = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deletePlan(plan.id) }) {
                            Icon(Icons.Default.Delete, "Usuń", modifier = Modifier.size(18.dp), tint = Color.Red)
                        }
                    }
                }
                
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("USTAWIENIA") },
                    selected = showSettingsDialog,
                    onClick = { showSettingsDialog = true; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.creator_info), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.rights_reserved), fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                    CenterAlignedTopAppBar(
                        title = { Text("PRO-TRACKER", color = Color.White, fontWeight = FontWeight.Black) },
                        navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null, tint = Color.White) } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color.White)
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = selectedTab == index, onClick = { selectedTab = index; viewingWorkoutDetails = null }, text = { Text(title, fontSize = 12.sp) })
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                if (viewingWorkoutDetails != null) {
                    WorkoutDetailsScreen(viewingWorkoutDetails!!) { viewingWorkoutDetails = null }
                } else {
                    when (selectedTab) {
                        0 -> ProfileScreen(viewModel) { viewingWorkoutDetails = it }
                        1 -> {
                            if (selectedMuscleGroup == null) ExercisesGrid { selectedMuscleGroup = it }
                            else ExerciseListScreen(selectedMuscleGroup!!, viewModel, { selectedMuscleGroup = null }, { showAddCustomDialog = it }) { ex ->
                                val groupName = context.getString(ex.muscleGroupRes)
                                val newPlan = TrainingPlan(initialName = "Plan $groupName").apply {
                                    exercises.add(PlanExercise(ex))
                                }
                                viewModel.savedPlans.add(newPlan)
                                viewModel.startWorkoutFromPlan(newPlan)
                                selectedTab = 2
                            }
                        }
                        2 -> WorkoutPlanView(viewModel) { showSwapDialog = true }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("USTAWIENIA") },
            text = {
                Column {
                    Text("WYBIERZ MOTYW", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    ThemeRow("Jasny", AppTheme.LIGHT, viewModel)
                    ThemeRow("Ciemny", AppTheme.DARK, viewModel)
                    ThemeRow("Systemowy", AppTheme.SYSTEM, viewModel)
                }
            },
            confirmButton = { TextButton(onClick = { showSettingsDialog = false }) { Text("ZAMKNIJ") } }
        )
    }

    if (showRenameDialog != null) {
        var newName by remember { mutableStateOf(showRenameDialog!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Zmień nazwę planu") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = { Button(onClick = { viewModel.renamePlan(showRenameDialog!!.id, newName); showRenameDialog = null }) { Text("ZAPISZ") } }
        )
    }

    if (showAddCustomDialog != null) {
        var cName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = null },
            title = { Text("Dodaj własne ćwiczenie") },
            text = { OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("Nazwa ćwiczenia") }) },
            confirmButton = { Button(onClick = { if(cName.isNotBlank()){ viewModel.addCustomExercise(cName, showAddCustomDialog!!); showAddCustomDialog = null } }) { Text("DODAJ") } }
        )
    }

    if (showSwapDialog) {
        val currentEx = viewModel.activeSession?.exercises?.get(viewModel.activeSession!!.currentExerciseIndex)
        val sameGroup = viewModel.allExercises.filter { it.muscleGroupRes == currentEx?.exercise?.muscleGroupRes }
        AlertDialog(
            onDismissRequest = { showSwapDialog = false },
            title = { Text("Zmień bieżące ćwiczenie") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(sameGroup) { ex ->
                        ListItem(headlineContent = { Text(ex.name) }, modifier = Modifier.clickable {
                            viewModel.activeSession?.let { session ->
                                session.exercises[session.currentExerciseIndex] = PlanExercise(ex)
                                session.registerExercise(ex) // Rejestrujemy to ćwiczenie w sesji
                            }
                            showSwapDialog = false
                        })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSwapDialog = false }) { Text("ANULUJ") } }
        )
    }
}

@Composable
fun WorkoutPlanView(viewModel: WorkoutViewModel, onSwap: () -> Unit) {
    val session = viewModel.activeSession ?: return EmptyWorkoutPlaceholder()
    val currentEx = session.exercises[session.currentExerciseIndex]
    val loggedSets = session.loggedSets[currentEx.exercise.id]!!

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(currentEx.exercise.name.uppercase(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 14.sp)
            IconButton(onClick = onSwap) { Icon(Icons.Default.SwapHoriz, null) }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
            if (!currentEx.exercise.isCustom && currentEx.exercise.videoResName != null) {
                Text("WIDEO: ${currentEx.exercise.videoResName}", color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                Icon(Icons.Default.VisibilityOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp).align(Alignment.Center))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Column(modifier = Modifier.weight(0.6f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { viewModel.addSetToActive() }) { Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(32.dp)) }
                    IconButton(onClick = { viewModel.removeSetFromActive() }) { Icon(Icons.Default.RemoveCircle, null, modifier = Modifier.size(32.dp), tint = Color.Gray) }
                }
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(loggedSets) { set -> EditableSetRow(set) }
                }
            }
            Column(modifier = Modifier.weight(0.4f).padding(start = 8.dp)) {
                WorkoutSideBtn("REKORDY")
                WorkoutSideBtn("OSTATNIE")
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CZAS", fontSize = 10.sp)
                        Text(String.format(Locale.getDefault(), "%02d:%02d", viewModel.elapsedSeconds / 60, viewModel.elapsedSeconds % 60), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                IconButton(onClick = { session.isPaused = !session.isPaused }, modifier = Modifier.fillMaxWidth().height(48.dp).background(if(session.isPaused) Color.Green else Color.Red, RoundedCornerShape(12.dp))) {
                    Icon(if(session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.finishWorkout() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("KONIEC") }
            }
        }
    }
}

@Composable
fun EditableSetRow(set: ExerciseSet) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = set.reps, 
            onValueChange = { set.reps = it }, 
            modifier = Modifier.width(55.dp), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, textAlign = TextAlign.Center)
        )
        Text(" x ", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = set.weight, 
            onValueChange = { set.weight = it }, 
            modifier = Modifier.weight(1f), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, textAlign = TextAlign.Center)
        )
        Checkbox(checked = set.isCompleted, onCheckedChange = { set.isCompleted = it }, colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue))
    }
}

@Composable
fun ProfileScreen(viewModel: WorkoutViewModel, onWorkoutClick: (SavedWorkout) -> Unit) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var tempGoal by remember { mutableStateOf(viewModel.userProfile.currentGoal) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("PROFIL", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
        item {
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("DNI TRENINGU: ${viewModel.getDaysSinceJoined()}", fontWeight = FontWeight.Bold)
                    Text("CEL: ${viewModel.userProfile.currentGoal}", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showGoalDialog = true }, shape = RoundedCornerShape(8.dp)) {
                        Text("ZMIEŃ CEL")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)); Text("HISTORIA TRENINGÓW", fontWeight = FontWeight.Black) }
        if (viewModel.workoutHistory.isEmpty()) { item { Text("Brak treningów", color = Color.Gray) } }
        else { items(viewModel.workoutHistory.reversed()) { workout ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onWorkoutClick(workout) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                ListItem(
                    headlineContent = { Text(workout.planName, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${workout.date} • ${workout.totalVolume.toInt()} kg") },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        } }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Zmień cel") },
            text = { OutlinedTextField(value = tempGoal, onValueChange = { tempGoal = it }) },
            confirmButton = { Button(onClick = { viewModel.userProfile = viewModel.userProfile.copy(currentGoal = tempGoal); showGoalDialog = false }) { Text("ZAPISZ") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(workout: SavedWorkout, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(title = { Text("SZCZEGÓŁY", fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } })
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text(workout.planName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("DATA: ${workout.date} | CZAS: ${workout.duration}", color = Color.Gray)
                Text("OBJĘTOŚĆ: ${workout.totalVolume.toInt()} kg", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Text("WYKRES OBJĘTOŚCI (SESJA)", fontWeight = FontWeight.Bold)
                VolumeChart(workout.exercisesData)
                Spacer(Modifier.height(24.dp))
            }
            items(workout.exercisesData) { ex ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(ex.name, fontWeight = FontWeight.Black, color = PrimaryBlue)
                        ex.sets.forEach { set ->
                            Text("Seria ${set.setNumber}: ${set.reps} x ${set.weight}kg", fontSize = 14.sp)
                        }
                        val avg = if(ex.sets.isNotEmpty()) ex.sets.mapNotNull { it.weight.toDoubleOrNull() }.average() else 0.0
                        Text("Średni ciężar: ${String.format(Locale.getDefault(), "%.1f", avg)} kg", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeChart(data: List<ExerciseHistoryData>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val barWidth = size.width / (data.size * 2).coerceAtLeast(1)
        val maxVol = data.map { ex -> ex.sets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0) } }.maxOrNull() ?: 1.0
        data.forEachIndexed { i, ex ->
            val vol = ex.sets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0) }
            val barHeight = (vol / maxVol) * size.height
            drawRect(PrimaryBlue, Offset(i * 2 * barWidth + barWidth/2, (size.height - barHeight).toFloat()), Size(barWidth, barHeight.toFloat()))
        }
    }
}

@Composable
fun ThemeRow(label: String, theme: AppTheme, viewModel: WorkoutViewModel) {
    Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.currentTheme = theme }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = viewModel.currentTheme == theme, onClick = { viewModel.currentTheme = theme })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ExercisesGrid(onGroupClick: (Int) -> Unit) {
    val groups = listOf(R.string.cat_chest to R.drawable.part_chest, R.string.cat_back to R.drawable.part_back, R.string.cat_legs to R.drawable.part_legs, R.string.cat_shoulders to R.drawable.part_shoulders, R.string.cat_biceps to R.drawable.part_biceps, R.string.cat_triceps to R.drawable.part_triceps, R.string.cat_core to R.drawable.part_core, R.string.cat_forearms to R.drawable.part_forearms, R.string.cat_glutes to R.drawable.part_glutes)
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().padding(12.dp)) {
        items(groups) { g -> Column(modifier = Modifier.padding(8.dp).clickable { onGroupClick(g.first) }, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(g.second), null, Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
            Text(stringResource(g.first), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        } }
    }
}

@Composable
fun ExerciseListScreen(muscleGroupRes: Int, viewModel: WorkoutViewModel, onBack: () -> Unit, onAdd: (Int) -> Unit, onExClick: (Exercise) -> Unit) {
    val filtered = viewModel.allExercises.filter { it.muscleGroupRes == muscleGroupRes }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text(stringResource(muscleGroupRes), Modifier.weight(1f), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onAdd(muscleGroupRes) }) { Icon(Icons.Default.Add, null) }
        }
        LazyColumn { items(filtered) { ex -> ListItem(headlineContent = { Text(ex.name) }, modifier = Modifier.clickable { onExClick(ex) }) } }
    }
}

@Composable
fun EmptyWorkoutPlaceholder() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Wybierz trening", color = Color.Gray) } }

@Composable
fun WorkoutSideBtn(label: String) {
    Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(38.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
