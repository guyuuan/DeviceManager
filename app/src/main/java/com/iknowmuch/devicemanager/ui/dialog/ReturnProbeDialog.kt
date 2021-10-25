package com.iknowmuch.devicemanager.ui.dialog

import android.widget.EditText
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.mqtt.MQTTStatus
import com.iknowmuch.devicemanager.ui.scene.home.HomeViewModel
import com.iknowmuch.devicemanager.ui.theme.DefaultBlackTextColor
import kotlinx.coroutines.delay
import me.pqpo.librarylog4a.Log4a

/**
 *@author: Chen
 *@createTime: 2021/10/20 9:26
 *@description:
 **/

private const val TAG = "ReturnProbeDiaLog4a"

@ExperimentalUnsignedTypes
@ExperimentalComposeUiApi
@Composable
fun ReturnProbeDialog(homeViewModel: HomeViewModel, onDismissRequest: () -> Unit) {
    val result by homeViewModel.returnResult
    if (result.first == 200) {
        ReturnSuccess(onDismissRequest)
    } else {
        ReturnFailed(message = result.second, onDismissRequest)
    }
}

@ExperimentalComposeUiApi
@Composable
fun ReturnSuccess(onDismissRequest: () -> Unit) {
    AutoCloseDialog(modifier = Modifier.fillMaxWidth(), onDismissRequest = onDismissRequest) {
        AutoCloseColumn(
            time = 10,
            modifier = Modifier.fillMaxWidth(),
            onCountdownEnd = { onDismissRequest() }) {
            Column(
                modifier = Modifier.padding(vertical = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_success),
                    contentDescription = "success"
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "归还成功",
                    style = MaterialTheme.typography.h6,
                    color = DefaultBlackTextColor
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ReturnFailed(message: String, onDismissRequest: () -> Unit) {
    AutoCloseDialog(modifier = Modifier.fillMaxWidth(), onDismissRequest = onDismissRequest) {
        AutoCloseColumn(
            showCountdown = false,
            time = 10,
            modifier = Modifier.fillMaxWidth(),
            onCountdownEnd = { onDismissRequest() }) {
            AutoCloseDialog(onDismissRequest = onDismissRequest) {
                AutoCloseColumn(time = 5, modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), onCountdownEnd = { onDismissRequest() }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        fontSize = 36.sp,
                        color = DefaultBlackTextColor
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalUnsignedTypes
@Composable
fun ScanView(mqStatus: MQTTStatus, viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    var showOffline by remember {
        mutableStateOf(false)
    }
    var scanResult by remember {
        mutableStateOf("")
    }
    var clearScanResult by remember {
        mutableStateOf(false)
    }
    AndroidView(factory = { cxt ->
        EditText(cxt).apply {
            addTextChangedListener {
                scanResult = it.toString()
            }
            requestFocus()
        }
    }, modifier = modifier.graphicsLayer {
        alpha = 0f
    }) {
        if (clearScanResult) {
            it.editableText.clear()
            clearScanResult = false
        }
    }
    if (showOffline) NetworkErrorDialog {
        showOffline = false
    }
    LaunchedEffect(key1 = scanResult) {
        delay(500)
        if (scanResult.isNotEmpty()) {
            Log4a.d(TAG, "ReturnProbeDiaLog4a: $scanResult")
            if (mqStatus != MQTTStatus.CONNECT_SUCCESS) showOffline = true
            viewModel.returnProbe(scanResult.removePrefix("\n"))
            clearScanResult = true
        }
    }
}