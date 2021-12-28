package com.iknowmuch.devicemanager.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.iknowmuch.devicemanager.BuildConfig
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.repository.WeiXinRepository
import com.iknowmuch.devicemanager.ui.LocalInsetsController
import com.iknowmuch.devicemanager.ui.scene.home.WeiXinViewModel
import kotlinx.coroutines.delay

/**
 *@author: Chen
 *@createTime: 2021/10/8 11:16
 *@description:
 **/
@ExperimentalComposeUiApi
@Composable
fun AutoCloseDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        val insetsController = LocalInsetsController.current
        LaunchedEffect(key1 = Unit) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        Surface(
            modifier = modifier, color = MaterialTheme.colors.background,
            shape = MaterialTheme.shapes.large
        ) {
            content()
        }
    }
}

@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun WXQRCodeDialog(
    onDismissRequest: () -> Unit,
) {
    val viewModel = hiltViewModel<WeiXinViewModel>()
    val qrCode by viewModel.qrCode.collectAsState()
    AutoCloseDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        AutoCloseColumn(
            time = 60,
            modifier = Modifier
                .fillMaxSize(),
            onCountdownEnd = { onDismissRequest() },
        ) {
            AnimatedContent(
                targetState = qrCode,
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { state ->
                when (state) {
                    is WeiXinRepository.DownloadResult.Failed -> {
                        Text(text = state.error.toString())
                    }
                    is WeiXinRepository.DownloadResult.Progress -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(100.dp),
                                strokeWidth = 8.dp
                            )
                        }
                    }
                    is WeiXinRepository.DownloadResult.Success -> {
                        Image(
                            painter = rememberImagePainter(data = state.file),
                            modifier = Modifier
                                .padding(vertical = 75.dp)
                                .size(415.dp),
                            contentDescription = "QRCode"
                        )
                    }
                }
            }
        }

    }
}

@ExperimentalComposeUiApi
@Composable
fun NetworkErrorDialog(onDismissRequest: () -> Unit) {
    AutoCloseDialog(onDismissRequest = onDismissRequest) {
        AutoCloseColumn(
            time = 5,
            modifier = Modifier.fillMaxWidth(),
            onCountdownEnd = { onDismissRequest() }) {
            Column(
                Modifier
                    .padding(top = 43.dp, bottom = 28.dp)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_offline),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(54.dp))
                Text(text = "当前网络不可用，请检查网络设置", fontSize = 28.sp, color = Color(0xFF838383))
            }
        }
    }
}

@Composable
fun AutoCloseColumn(
    showCountdown: Boolean = true,
    time: Int,
    modifier: Modifier,
    onCountdownEnd: () -> Unit,
    content: @Composable () -> Unit
) {
    var countdown by remember {
        mutableStateOf(time)
    }
    Box(contentAlignment = Alignment.BottomEnd) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            if (showCountdown) {
                Row(
                    Modifier
                        .padding(top = 30.dp)
                        .padding(horizontal = 34.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.text_auto_close_time).format(countdown),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.primary
                    )
                    IconButton(onClick = { onCountdownEnd() }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "close"
                        )
                    }
                }
            }
            content()
        }
        if (BuildConfig.DEBUG) Text(text = "倒计时 $countdown s")
    }

    LaunchedEffect(key1 = countdown) {
        delay(1000)
        countdown = (countdown - 1).coerceAtLeast(0)
        if (countdown == 0) {
            onCountdownEnd()
        }
    }
}