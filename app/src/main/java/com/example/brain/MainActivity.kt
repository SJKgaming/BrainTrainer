package com.example.brain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

enum class Screen { HOME, GAME }
val keypadButtons = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓")
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
    when (screen) {
        Screen.HOME -> HomeScreen(onStart = { screen = Screen.GAME })
        Screen.GAME -> BrainApp(onBack = { screen = Screen.HOME })
    }
}

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF07111F), Color(0xFF102B52), Color(0xFF081120))))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun HomeScreen(onStart: () -> Unit) {
    AppBackground {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Calculate, null, tint = Color(0xFF4DA3FF), modifier = Modifier.size(100.dp))
            Spacer(Modifier.height(20.dp))
            Text("Brain Trainer", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(50.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(Color.White.copy(0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Choose Training", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(25.dp))
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))), RoundedCornerShape(22.dp))
                    ) {
                        Text("➕ Addition", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BrainApp(onBack: () -> Unit) {
    var level by remember { mutableIntStateOf(1) }
    var streak by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Ready? 🚀") }
    var timeLeft by remember { mutableIntStateOf(5) }
    var currentQuestion by remember { mutableStateOf(generateQuestion(level)) }
    val isCorrect = remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val progress by animateFloatAsState(timeLeft / 5f, tween(300), label = "")
    val questionScale by animateFloatAsState(if (isCorrect.value) 1.1f else 1f, tween(150), label = "")

    LaunchedEffect(isCorrect.value) {
        if (isCorrect.value) {
            delay(200)
            isCorrect.value = false
        }
    }

    LaunchedEffect(currentQuestion) {
        timeLeft = 5
        userAnswer = ""
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        isCorrect.value = false
        message = "⏰ Too Slow!"
        streak = 0
        currentQuestion = generateQuestion(level)
    }

    fun submitAnswer() {
        if (userAnswer.toIntOrNull() == currentQuestion.answer) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isCorrect.value = true
            streak++
            xp += 25
            if (streak >= 5) {
                level++
                streak = 0
                message = "🚀 Level Up!"
            } else {
                message = "✅ Correct!"
            }
        } else {
            isCorrect.value = false
            message = "❌ Wrong! Answer = ${currentQuestion.answer}"
            streak = 0
        }
        userAnswer = ""
        currentQuestion = generateQuestion(level)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4DA3FF), secondary = Color(0xFF06B6D4),
            background = Color(0xFF07111F), surface = Color(0xFF111827)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF07111F), Color(0xFF102B52), Color(0xFF081120))))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onBack) { Text("← Back", color = Color.White) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(Color.White.copy(0.08f)),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, tint = Color.Yellow)
                                Spacer(Modifier.width(8.dp))
                                Text("Level $level", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("XP $xp", color = Color(0xFF06B6D4), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(14.dp))
                        Text("🔥 Combo: $streak / 5", color = Color(0xFF4DA3FF), fontSize = 18.sp)
                        Spacer(Modifier.height(25.dp))

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress }, modifier = Modifier.size(90.dp), strokeWidth = 8.dp,
                                color = Color(0xFF4DA3FF), trackColor = Color.White.copy(0.1f)
                            )
                            Text("$timeLeft", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(30.dp))
                        AnimatedContent(
                            targetState = currentQuestion,
                            transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) },
                            label = ""
                        ) { q ->
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                Text(q.text, modifier = Modifier.scale(questionScale), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(70.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(Color.Black.copy(0.3f)),
                            border = BorderStroke(1.dp, Color(0xFF4DA3FF))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(userAnswer.ifEmpty { "_" }, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(25.dp))

                        Column {
                            keypadButtons.chunked(3).forEach { row ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { value ->
                                        Button(
                                            onClick = {
                                                when (value) {
                                                    "⌫" -> if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                                                    "✓" -> submitAnswer()
                                                    else -> if (userAnswer.length < 8) userAnswer += value
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(62.dp),
                                            shape = RoundedCornerShape(22.dp),
                                            colors = ButtonDefaults.buttonColors(Color.White.copy(0.08f)),
                                            border = BorderStroke(1.dp, Color.White.copy(0.1f))
                                        ) {
                                            if (value == "⌫") {
                                                Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = Color.White)
                                            } else {
                                                Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(message, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHome() { MainScreen() }

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGame() { BrainApp(onBack = {}) }