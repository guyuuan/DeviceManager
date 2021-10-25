package com.iknowmuch.devicemanager.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsCompat
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.bean.ControllerResult
import com.iknowmuch.devicemanager.ui.LocalInsetsController
import com.iknowmuch.devicemanager.ui.theme.DefaultBlackTextColor
import com.iknowmuch.devicemanager.ui.theme.ThemeBlue

/**
 *@author: Chen
 *@createTime: 2021/10/22 15:54
 *@description:
 **/
@ExperimentalComposeUiApi
@Composable
fun UsingDialog(data: ControllerResult, onDismissRequest: () -> Unit) {
    if (data.doorNo == 0) return
    when {
        data.openState && data.closeState == null -> OpenSuccess(data = data)
        !data.openState && data.closeState == null -> OpenFailed(onDismissRequest)
        data.closeState == true -> CloseSuccess(data, onDismissRequest)
        data.closeState == false -> CloseFailed(data, onDismissRequest)
    }
}

@ExperimentalComposeUiApi
@Composable
fun CloseFailed(data: ControllerResult, onDismissRequest: () -> Unit) {
    val image: Int
    val message: String
    when {
        data.status == 1 && data.probeState == true ||
        data.status == 0 && data.probeState == false -> {
            image = R.drawable.ic_error
            message = stringResource(R.string.text_door_is_open)
        }
        else -> {
            image = R.drawable.ic_fialed
            message =
                stringResource(id = if (data.status == 0) R.string.text_borrow_failed else R.string.text_return_failed)
        }
    }
    AutoCloseDialog(onDismissRequest = onDismissRequest) {
        AutoCloseColumn(time = 5, modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), onCountdownEnd = { onDismissRequest() }) {
            Spacer(modifier = Modifier.height(86.dp))
            Image(painter = painterResource(id = image), contentDescription = null)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 36.sp,
                color = DefaultBlackTextColor
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun CloseSuccess(data: ControllerResult, onDismissRequest: () -> Unit) {
    val image: Int
    val message: String
    when {
        (data.status == 0 && data.probeState == true) -> {
            image = R.drawable.ic_error
            message = "设备未拿走"
        }
        data.status == 1 && data.probeState == false ->{
            image = R.drawable.ic_error
            message = "设备未放入"
        }
        else -> {
            image = R.drawable.ic_success
            message =
                stringResource(id = if (data.status == 0) R.string.text_borrow_success else R.string.text_return_success)
        }
    }
    AutoCloseDialog(onDismissRequest = onDismissRequest) {
        AutoCloseColumn(time = 5, modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), onCountdownEnd = { onDismissRequest() }) {
            Spacer(modifier = Modifier.height(86.dp))
            Image(painter = painterResource(id = image), contentDescription = null)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 36.sp,
                color = DefaultBlackTextColor
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun OpenSuccess(data: ControllerResult) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    color = MaterialTheme.colors.background,
                    shape = MaterialTheme.shapes.large
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(if (data.status == 0) R.string.text_borrowing else R.string.text_returning),
                fontSize = 32.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium, modifier = Modifier
                    .padding(top = 30.dp, start = 30.dp)
                    .fillMaxWidth()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.doorNo.toString(),
                    fontSize = 90.sp,
                    color = ThemeBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "此柜门已打开",
                    fontSize = 36.sp,
                    color = DefaultBlackTextColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(60.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_step_line),
                        contentDescription = null,
                        modifier = Modifier.size(width = 20.dp, height = 98.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = stringResource(id = if (data.status == 0) R.string.text_borrow_tip else R.string.text_return_tip),
                            fontSize = 36.sp,
                            color = Color(0xFFF22D2D)
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(text = "关闭柜门", fontSize = 36.sp, color = Color(0xFFF22D2D))
                    }
                }
            }
        }
        val insetsController = LocalInsetsController.current
        LaunchedEffect(key1 = Unit) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun OpenFailed(onDismissRequest: () -> Unit) {
    AutoCloseDialog(onDismissRequest = onDismissRequest) {
        AutoCloseColumn(time = 5, modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), onCountdownEnd = { onDismissRequest() }) {
            Spacer(modifier = Modifier.height(86.dp))
            Image(painter = painterResource(id = R.drawable.ic_fialed), contentDescription = null)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "柜门打开失败")
        }
    }
}