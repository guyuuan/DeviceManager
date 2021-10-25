package com.iknowmuch.devicemanager.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

//val Purple200 = Color(0xFFBB86FC)
//val Purple500 = Color(0xFF6200EE)
//val Purple700 = Color(0xFF3700B3)
//val Teal200 = Color(0xFF03DAC5)
val ThemeBlue = Color(0xFF4874FF)
val SecondaryGreen = Color(0xFF24BD84)
val CorrectBlue = Color(0xFF36B6FF)
val ErrorRed = Color(0xFFFF0036)

val GreenBrush = Brush.linearGradient(
    listOf(Color(0xFF24BD84), Color(0xFF4CC777)),
    start = Offset.Zero, end = Offset.Infinite
)
val UnabledBrush = Brush.linearGradient(
    listOf(Color(0xFFE9E9E9), Color(0xFFE3E3E3)),
    start = Offset.Zero, end = Offset.Infinite
)

val BlueBrush = Brush.linearGradient(
    listOf(Color(0xFF649AFD), Color(0xFF82A0FF)),
    start = Offset.Zero, end = Offset.Infinite
)

val DefaultBlackTextColor = Color(0xFF242424)

val BorderBrush = Brush.verticalGradient(listOf(ThemeBlue, ThemeBlue.copy(alpha = 0f)))

val BatteryColor = Color(0xFF1EE537)