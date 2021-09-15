package com.iknowmuch.devicemanager.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

/**
 *@author: Chen
 *@createTime: 2021/8/13 20:18
 *@description:
 **/

val LocalNavController =
    compositionLocalOf<NavHostController> { error("Can't get nacController") }

