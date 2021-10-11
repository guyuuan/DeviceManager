package com.iknowmuch.devicemanager.ui.scene.scan

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.ui.LocalNavController
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 *@author: Chen
 *@createTime: 2021/10/11 15:18
 *@description:
 **/
@Composable
fun ScanScene(navController: NavController = LocalNavController.current) {
    val scanSize = with(LocalDensity.current) { 852.dp.toPx() }
    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    var remoteView :RemoteView? = null
    var isScanning by remember {
        mutableStateOf(true)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(factory = { cxt ->
            RemoteView.Builder()
                .setContext(cxt as Activity)
                .setBoundingBox(
                    Rect(
                        (screenWidth / 2 - scanSize / 2).roundToInt(),
                        (screenHeight / 2 - scanSize / 2).roundToInt(),
                        (screenWidth / 2 + scanSize / 2).roundToInt(),
                        (screenHeight / 2 + scanSize / 2).roundToInt()
                    )
                )
                .setFormat(HmsScan.ALL_SCAN_TYPE)
                .build().also {
                    remoteView = it
                    it.setOnResultCallback { result ->
                        if (!isScanning) return@setOnResultCallback
                        if (result.isNotEmpty()) {
                            isScanning = false
                            Toast.makeText(cxt, result.first().originalValue, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    it.onCreate(null)
                    it.onStart()
                }
        }, modifier = Modifier.fillMaxSize()) {

        }
        Image(
            painter = painterResource(id = R.drawable.scan_board),
            contentDescription = "scan board",
            modifier = Modifier.size(852.dp)
        )
    }
    LaunchedEffect(key1 = isScanning) {
        if (!isScanning) {
            delay(1000)
            isScanning = true
        }
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            remoteView?.onStop()
        }
    }
}