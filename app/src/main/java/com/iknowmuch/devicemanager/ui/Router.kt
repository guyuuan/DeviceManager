package com.iknowmuch.devicemanager.ui

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.iknowmuch.devicemanager.ui.scene.home.HomeScene
import com.iknowmuch.devicemanager.ui.scene.loading.LoadingScene
import com.iknowmuch.devicemanager.ui.scene.more.MoreScene
import com.iknowmuch.devicemanager.ui.scene.scan.ScanScene

/**
 *@author: Chen
 *@createTime: 2021/8/31 13:18
 *@description:
 **/
sealed class Scene(val id: String, @StringRes val label: Int? = null) {
    object Loading : Scene(id = "Loading")
    object Home : Scene(id = "home")
    object More : Scene(id = "Log4ain")
    object Scan : Scene(id = "scan")
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@Composable
fun Router(
    navController: NavHostController = rememberAnimatedNavController(),
) {
    CompositionLocalProvider(
        LocalNavController provides navController,
    ) {
        ProvideWindowInsets {
            AnimatedNavHost(
                navController = navController,
                startDestination = Scene.Loading.id
            ) {
                route()
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
private fun NavGraphBuilder.route() {
    composable(Scene.More.id) {
        MoreScene()
    }
    composable(Scene.Home.id) {
        HomeScene()
    }
    composable(Scene.Loading.id) {
        LoadingScene()
    }
    composable(Scene.Scan.id) {
        ScanScene()
    }
}