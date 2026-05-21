package com.example.brain

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

// --- Global Theme & Styling Constants ---
private val BluePrimary = Color(0xFF4DA3FF)
private val CyanSecondary = Color(0xFF06B6D4)
private val DarkBg1 = Color(0xFF07111F)
private val DarkBg2 = Color(0xFF102B52)
private val DarkBg3 = Color(0xFF081120)

private val BgBrush = Brush.linearGradient(listOf(DarkBg1, DarkBg2, DarkBg3))
private val CardBackgroundColor = Color.White.copy(alpha = 0.08f)
private val CardBorderColor = Color.White.copy(alpha = 0.1f)

private val AppColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = CyanSecondary,
    background = DarkBg1,
    surface = Color(0xFF111827)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColorScheme) {
                MainScreen()
            }
        }
    }
}

enum class Screen { HOME, GAME, HIGHSCORE }

val keypadButtons = listOf(
    "1", "2", "3",
    "4", "5", "6",
    "7", "8", "9",
    "⌫", "0", "✓"
)

data class Question(val text: String, val answer: Int)

fun generateQuestion(level: Int): Question {
    val range = when (level) {
        1 -> 1..10
        2 -> 10..50
        3 -> 50..100
        4 -> 100..500
        5 -> 500..1000
        else -> 1000..5000
    }
    val n1 = range.random()
    val n2 = range.random()
    return Question("$n1 + $n2", n1 + n2)
}

@Composable
fun MainScreen() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    val highLevel by dataStore.highLevelFlow.collectAsState(initial = 1)
    val highCombo by dataStore.highComboFlow.collectAsState(initial = 0)

    when (screen) {
        Screen.HOME -> HomeScreen(
            onStart = { screen = Screen.GAME },
            onHighScore = { screen = Screen.HIGHSCORE }
        )
        Screen.GAME -> BrainApp(
            onBack = { screen = Screen.HOME },
            onNewHighScore = { level, combo ->
                if (level > highLevel || (level == highLevel && combo > highCombo)) {
                    scope.launch {
                        dataStore.saveHighScore(level, combo)
                    }
                }
            }
        )
        Screen.HIGHSCORE -> HighScoreScreen(
            highLevel = highLevel,
            highCombo = highCombo,
            onBack = { screen = Screen.HOME }
        )
    }
}

// --- Reusable UI Components ---

@Composable
fun AppBackground(
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBrush)
            .padding(20.dp),
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    padding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(CardBackgroundColor),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// --- Screens ---

@Composable
fun HomeScreen(onStart: () -> Unit, onHighScore: () -> Unit) {
    AppBackground {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text("Brain Trainer", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(50.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Choose Training", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(25.dp))

                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF3B82F6), CyanSecondary)),
                            RoundedCornerShape(22.dp)
                        )
                ) {
                    Text("➕ Addition", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onHighScore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BluePrimary)
                ) {
                    Text("🏆 High Score", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BrainApp(onBack: () -> Unit, onNewHighScore: (Int, Int) -> Unit) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    var level by remember { mutableIntStateOf(1) }
    var streak by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Ready? 🚀") }
    var timeLeft by remember { mutableIntStateOf(5) }
    var currentQuestion by remember { mutableStateOf(generateQuestion(level)) }
    var isCorrect by remember { mutableStateOf(false) }

    // FIX 1: flag to prevent timer penalty when user already submitted
    var isAnswered by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(targetValue = timeLeft / 5f, animationSpec = tween(300), label = "progress")
    val questionScale by animateFloatAsState(targetValue = if (isCorrect) 1.1f else 1f, animationSpec = tween(150), label = "scale")

    LaunchedEffect(isCorrect) {
        if (isCorrect) {
            delay(200)
            isCorrect = false
        }
    }

    LaunchedEffect(currentQuestion) {
        timeLeft = 5
        userAnswer = ""
        isAnswered = false

        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }

        // FIX 1: only penalize if the user didn't already submit an answer
        if (!isAnswered) {
            message = "⏰ Too Slow!"
            streak = 0
            currentQuestion = generateQuestion(level)
        }
    }

    fun vibrate(duration: Long) {
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun submitAnswer() {
        // FIX 1: mark as answered immediately to block timer penalty
        isAnswered = true

        if (userAnswer.toIntOrNull() == currentQuestion.answer) {
            vibrate(100)
            isCorrect = true
            streak++

            // FIX 3: apply level-up BEFORE saving high score so correct level is recorded
            if (streak >= 5) {
                level++
                streak = 0
                message = "🚀 Level Up!"
            } else {
                message = "✅ Correct!"
            }
            onNewHighScore(level, streak)

        } else {
            vibrate(300)
            isCorrect = false
            message = "❌ Wrong! Answer = ${currentQuestion.answer}"
            streak = 0
        }

        userAnswer = ""
        currentQuestion = generateQuestion(level)
    }

    AppBackground(contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onBack) { Text("← Back", color = Color.White) }

            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
                // Header
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text("Level $level", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("🔥 Combo: $streak / 5", color = BluePrimary, fontSize = 18.sp)
                Spacer(Modifier.height(25.dp))

                // Timer
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(90.dp),
                        color = BluePrimary,
                        strokeWidth = 8.dp,
                        trackColor = CardBorderColor
                    )
                    Text("$timeLeft", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(30.dp))

                // Question
                AnimatedContent(
                    targetState = currentQuestion,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "question_transition"
                ) { q ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            q.text,
                            modifier = Modifier.scale(questionScale),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Answer Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(Color.Black.copy(0.3f)),
                    border = BorderStroke(1.dp, BluePrimary)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(userAnswer.ifEmpty { "_" }, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(25.dp))

                // Keypad
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    keypadButtons.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { value ->
                                Button(
                                    onClick = {
                                        when (value) {
                                            "⌫" -> if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                                            "✓" -> submitAnswer()
                                            else -> if (userAnswer.length < 8) userAnswer += value
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(62.dp),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = ButtonDefaults.buttonColors(CardBackgroundColor),
                                    border = BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    if (value == "⌫") {
                                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = Color.White)
                                    } else {
                                        Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(message, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun HighScoreScreen(onBack: () -> Unit, highLevel: Int, highCombo: Int) {
    BackHandler { onBack() }

    AppBackground {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
            Spacer(Modifier.height(40.dp))
            Text("🏆 High Score", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(40.dp))

            GlassCard(shape = RoundedCornerShape(30.dp), padding = 40.dp) {
                Text("Level $highLevel", color = CyanSecondary, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                Text("Best Combo: $highCombo / 5 🔥", color = Color.White.copy(0.7f), fontSize = 18.sp)
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHome() {
    MaterialTheme(colorScheme = AppColorScheme) { MainScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGame() {
    MaterialTheme(colorScheme = AppColorScheme) { BrainApp(onBack = {}, onNewHighScore = { _, _ -> }) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHighScore() {
    MaterialTheme(colorScheme = AppColorScheme) { HighScoreScreen(highLevel = 4, highCombo = 3, onBack = {}) }
}