package com.iknowmuch.devicemanager.ui.scene.more

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.iknowmuch.devicemanager.TestView
import com.iknowmuch.devicemanager.ui.scene.home.TopBackground

/**
 *@author: Chen
 *@createTime: 2021/9/13 15:19
 *@description:
 **/
@Composable
fun MoreScene() {
    Box(modifier = Modifier.fillMaxSize()) {
        TopBackground()

        AndroidView(factory = { context -> TestView(context) }, Modifier.fillMaxSize())
    }
}