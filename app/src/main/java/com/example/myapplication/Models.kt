package com.example.myapplication

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.UUID

/**
 * Bazowy model ćwiczenia.
 */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroupRes: Int,
    val description: String = "",
    val videoResName: String? = null,
    val isCustom: Boolean = false
)

/**
 * Ćwiczenie przypisane do planu lub aktywnej sesji.
 */
data class PlanExercise(
    var exercise: Exercise,
    var setsCount: Int = 3,
    var targetReps: String = "8-12"
)

/**
 * Plan treningowy stworzony przez użytkownika.
 */
class TrainingPlan(
    val id: String = UUID.randomUUID().toString(),
    initialName: String,
    initialExercises: List<PlanExercise> = emptyList()
) {
    var name by mutableStateOf(initialName)
    val exercises = mutableStateListOf<PlanExercise>().apply { addAll(initialExercises) }
}

/**
 * Model serii w trakcie treningu. Pola są MutableState dla pełnej reaktywności UI.
 */
class ExerciseSet(
    val setNumber: Int,
    initialWeight: String = "0",
    initialReps: String = "0",
    initialCompleted: Boolean = false
) {
    var weight by mutableStateOf(initialWeight)
    var reps by mutableStateOf(initialReps)
    var isCompleted by mutableStateOf(initialCompleted)
}

/**
 * Model aktywnej sesji treningowej.
 * Śledzi wszystkie ćwiczenia użyte w sesji, w tym te po zmianie (SWAP).
 */
class ActiveWorkoutSession(
    val planId: String?,
    var planName: String,
    val date: String,
    initialExercises: List<PlanExercise>
) {
    var currentExerciseIndex by mutableIntStateOf(0)
    
    // Lista aktualnych ćwiczeń widocznych w sesji (może być modyfikowana przez SWAP)
    val exercises = mutableStateListOf<PlanExercise>().apply { addAll(initialExercises) }
    
    // Mapa logów: exerciseId -> lista serii
    val loggedSets = mutableMapOf<String, SnapshotStateList<ExerciseSet>>()
    
    // Rejestr nazw ćwiczeń (aby pamiętać nazwę nawet jeśli zostanie podmienione)
    val exerciseNamesRegistry = mutableMapOf<String, String>()
    
    // Lista ID wszystkich ćwiczeń, które wystąpiły w sesji (chronologia użycia)
    val sessionHistoryOrder = mutableStateListOf<String>()
    
    var isPaused by mutableStateOf(false)
    
    init {
        initialExercises.forEach { registerExercise(it.exercise) }
    }

    /**
     * Rejestruje ćwiczenie, aby jego dane nie zginęły po SWAP i trafiły do archiwum.
     */
    fun registerExercise(ex: Exercise) {
        if (!loggedSets.containsKey(ex.id)) {
            loggedSets[ex.id] = mutableStateListOf(ExerciseSet(1))
            exerciseNamesRegistry[ex.id] = ex.name
            sessionHistoryOrder.add(ex.id)
        }
    }
}

/**
 * Archiwalny rekord treningu (Statyczny).
 */
data class SavedWorkout(
    val id: String = UUID.randomUUID().toString(),
    val planName: String,
    val date: String,
    val duration: String,
    val durationSeconds: Long,
    val exercisesData: List<ExerciseHistoryData>,
    val totalVolume: Double
)

data class ExerciseHistoryData(
    val name: String,
    val sets: List<ExerciseHistorySet>
)

data class ExerciseHistorySet(
    val setNumber: Int,
    val weight: String,
    val reps: String
)

data class UserProfile(
    val name: String = "Marcin Skorzak",
    val joinDate: Long = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30),
    var currentGoal: String = "Zwiększyć siłę o 10%"
)

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}
