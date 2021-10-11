package com.iknowmuch.devicemanager.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy

/**
 *@author: Chen
 *@createTime: 2021/10/8 11:16
 *@description:
 **/
@ExperimentalComposeUiApi
@Composable
fun AutoCloseDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest,properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOff,usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .size(640.dp)
                .background(Color.White)
        ) {

        }
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
fun AutoCloseDialogPreview() {
    Surface(modifier = Modifier.fillMaxSize(),color = Color.White){
        AutoCloseDialog {

        }
    }
}