package com.example.brain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

        setContent {
            MainScreen()
        }
    }
}

enum class Screen {
    HOME,
    GAME
}

data class Question(
    val text: String,
    val answer: Int
)

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

    return Question(
        "$n1 + $n2",
        n1 + n2
    )
}

@Composable
fun MainScreen() {

    var screen by remember {
        mutableStateOf(Screen.HOME)
    }

    when (screen) {

        Screen.HOME -> HomeScreen(
            onStart = {
                screen = Screen.GAME
            }
        )

        Screen.GAME -> BrainApp(
            onBack = {
                screen = Screen.HOME
            }
        )
    }
}

@Composable
fun HomeScreen(
    onStart: () -> Unit
) {

    Box(

        modifier = Modifier

            .fillMaxSize()

            .background(

                Brush.linearGradient(

                    listOf(

                        Color(0xFF07111F),

                        Color(0xFF102B52),

                        Color(0xFF081120)
                    )
                )
            )

            .padding(20.dp),

        contentAlignment = Alignment.Center
    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(

                Icons.Default.Calculate,

                contentDescription = null,

                tint = Color(0xFF4DA3FF),

                modifier = Modifier.size(100.dp)
            )

            Spacer(
                Modifier.height(20.dp)
            )

            Text(

                text = "Brain Trainer",

                color = Color.White,

                fontSize = 38.sp,

                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                Modifier.height(50.dp)
            )

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(28.dp),

                colors = CardDefaults.cardColors(
                    Color.White.copy(0.08f)
                ),

                border = BorderStroke(
                    1.dp,
                    Color.White.copy(0.1f)
                )
            ) {

                Column(

                    modifier = Modifier.padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(

                        text = "Choose Training",

                        color = Color.White,

                        fontSize = 24.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(25.dp)
                    )

                    Button(

                        onClick = onStart,

                        shape = RoundedCornerShape(22.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),

                        modifier = Modifier

                            .fillMaxWidth()

                            .height(60.dp)

                            .background(

                                Brush.horizontalGradient(

                                    listOf(

                                        Color(0xFF3B82F6),

                                        Color(0xFF06B6D4)
                                    )
                                ),

                                RoundedCornerShape(22.dp)
                            )
                    ) {

                        Text(

                            text = "➕ Addition",

                            color = Color.White,

                            fontSize = 20.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrainApp(
    onBack: () -> Unit
) {

    var level by remember { mutableIntStateOf(1) }
    var streak by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(0) }

    var userAnswer by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("Ready? 🚀")
    }

    var timeLeft by remember {
        mutableIntStateOf(5)
    }

    var currentQuestion by remember {
        mutableStateOf(generateQuestion(level))
    }

    val isCorrect = remember {
        mutableStateOf(false)
    }

    val haptic = LocalHapticFeedback.current

    val progress by animateFloatAsState(
        timeLeft / 5f,
        tween(300),
        label = ""
    )

    val scale by animateFloatAsState(
        if (isCorrect.value) 1.05f else 1f,
        spring(),
        label = ""
    )

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

        currentQuestion =
            generateQuestion(level)
    }

    MaterialTheme(

        colorScheme = darkColorScheme(

            primary = Color(0xFF4DA3FF),

            secondary = Color(0xFF06B6D4),

            background = Color(0xFF07111F),

            surface = Color(0xFF111827)
        )
    ) {

        Box(

            modifier = Modifier

                .fillMaxSize()

                .background(

                    Brush.linearGradient(

                        listOf(

                            Color(0xFF07111F),

                            Color(0xFF102B52),

                            Color(0xFF081120)
                        )
                    )
                )

                .padding(20.dp)
        ) {

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Spacer(
                    Modifier.height(10.dp)
                )

                TextButton(
                    onClick = onBack
                ) {

                    Text(
                        "← Back",
                        color = Color.White
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                Row(

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Icon(

                        Icons.Default.Calculate,

                        contentDescription = null,

                        tint = Color(0xFF4DA3FF),

                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(
                        Modifier.width(16.dp)
                    )

                    Text(

                        "Brain Trainer",

                        color = Color.White,

                        fontSize = 34.sp,

                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(
                    Modifier.height(30.dp)
                )

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale),

                    shape = RoundedCornerShape(32.dp),

                    colors = CardDefaults.cardColors(
                        Color.White.copy(0.08f)
                    ),

                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(0.1f)
                    )
                ) {

                    Column(

                        modifier = Modifier.padding(24.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Row(

                            Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Row(

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.Bolt,
                                    null,
                                    tint = Color.Yellow
                                )

                                Spacer(
                                    Modifier.width(8.dp)
                                )

                                Text(

                                    "Level $level",

                                    color = Color.White,

                                    fontSize = 22.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }

                            Text(

                                "XP $xp",

                                color = Color(0xFF06B6D4),

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(14.dp)
                        )

                        Text(

                            "🔥 Combo: $streak / 5",

                            color = Color(0xFF4DA3FF),

                            fontSize = 18.sp
                        )

                        Spacer(
                            Modifier.height(25.dp)
                        )

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            CircularProgressIndicator(

                                progress = { progress },

                                modifier = Modifier.size(90.dp),

                                strokeWidth = 8.dp,

                                color = Color(0xFF4DA3FF),

                                trackColor =
                                    Color.White.copy(0.1f)
                            )

                            Text(

                                "$timeLeft",

                                color = Color.White,

                                fontSize = 28.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(30.dp)
                        )

                        AnimatedContent(

                            targetState = currentQuestion,

                            transitionSpec = {

                                (slideInVertically { it } + fadeIn())
                                    .togetherWith(
                                        slideOutVertically { -it } + fadeOut()
                                    )
                            },

                            label = ""
                        ) { q ->

                            Text(

                                q.text,

                                color = Color.White,

                                fontSize = 42.sp,

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }

                        Spacer(
                            Modifier.height(30.dp)
                        )

                        OutlinedTextField(

                            value = userAnswer,

                            onValueChange = {
                                userAnswer = it
                            },

                            singleLine = true,

                            label = {
                                Text("Enter Answer")
                            },

                            shape = RoundedCornerShape(20.dp),

                            modifier =
                                Modifier.fillMaxWidth(),

                            colors =
                                OutlinedTextFieldDefaults.colors(

                                    focusedBorderColor =
                                        Color(0xFF4DA3FF),

                                    unfocusedBorderColor =
                                        Color.Gray,

                                    focusedTextColor =
                                        Color.White,

                                    unfocusedTextColor =
                                        Color.White
                                )
                        )

                        Spacer(
                            Modifier.height(25.dp)
                        )

                        Button(

                            onClick = {

                                if (
                                    userAnswer.toIntOrNull()
                                    ==
                                    currentQuestion.answer
                                ) {

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )

                                    isCorrect.value = true

                                    streak++

                                    xp += 25

                                    if (streak >= 5) {

                                        level++

                                        streak = 0

                                        message =
                                            "🚀 Level Up!"

                                    } else {

                                        message =
                                            "✅ Correct!"
                                    }

                                } else {

                                    isCorrect.value = false

                                    message =
                                        "❌ Wrong! Answer = ${currentQuestion.answer}"

                                    streak = 0
                                }

                                userAnswer = ""

                                currentQuestion =
                                    generateQuestion(level)
                            },

                            shape =
                                RoundedCornerShape(22.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    Color.Transparent
                                ),

                            modifier = Modifier

                                .fillMaxWidth()

                                .height(60.dp)

                                .background(

                                    Brush.horizontalGradient(

                                        listOf(

                                            Color(0xFF3B82F6),

                                            Color(0xFF06B6D4)
                                        )
                                    ),

                                    RoundedCornerShape(22.dp)
                                )
                        ) {

                            Text(

                                "Submit",

                                color = Color.White,

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(22.dp)
                        )

                        Text(

                            message,

                            color = Color.White,

                            fontSize = 18.sp,

                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)

@Composable
fun PreviewHome() {

    MainScreen()
}

@Preview(
    showBackground = true,
    showSystemUi = true
)

@Composable
fun PreviewGame() {

    BrainApp(
        onBack = {}
    )
}