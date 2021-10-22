package com.iknowmuch.devicemanager.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController

/**
 *@author: Chen
 *@createTime: 2021/8/13 20:18
 *@description:
 **/

val LocalNavController =
    compositionLocalOf<NavHostController> { error("Can't get navController") }
val LocalInsetsController =
    compositionLocalOf<WindowInsetsControllerCompat> { error("Can't get WindowInsetsControllerCompat") }

