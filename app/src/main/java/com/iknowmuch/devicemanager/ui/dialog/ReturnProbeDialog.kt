package com.iknowmuch.devicemanager.ui.dialog

import android.util.Log
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.widget.addTextChangedListener
import com.iknowmuch.devicemanager.ui.scene.home.HomeViewModel
import com.iknowmuch.devicemanager.ui.theme.ThemeBlue
import kotlinx.coroutines.delay

/**
 *@author: Chen
 *@createTime: 2021/10/20 9:26
 *@description:
 **/

private const val TAG = "ReturnProbeDialog"

@ExperimentalComposeUiApi
@Composable
fun ReturnProbeDialog(homeViewModel: HomeViewModel, onDismissRequest: () -> Unit) {

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background, shape = MaterialTheme.shapes.large)
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "请将设备编码对准扫码框",
                    Modifier
                        .padding(vertical = 110.dp),
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Medium
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(color = Color(0xFFE7E7E7))
                )
                Text(
                    text = "请将设备编码对准扫码框",
                    Modifier
                        .padding(vertical = 40.dp),
                    style = MaterialTheme.typography.h5,
                    color = ThemeBlue,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }

}

@Composable
fun ScanView(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
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
    LaunchedEffect(key1 = scanResult) {
        delay(500)
        if (scanResult.isNotEmpty()) {
            Log.d(TAG, "ReturnProbeDialog: $scanResult")
            viewModel.returnProbe(scanResult)
            clearScanResult = true
        }
    }
}