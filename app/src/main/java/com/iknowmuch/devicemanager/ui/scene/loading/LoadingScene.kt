package com.iknowmuch.devicemanager.ui.scene.loading

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.mqtt.MQTTStatus
import com.iknowmuch.devicemanager.ui.LocalNavController
import com.iknowmuch.devicemanager.ui.Scene
import com.iknowmuch.devicemanager.ui.dialog.AppConfigDialog
import com.iknowmuch.devicemanager.ui.theme.CorrectBlue
import com.iknowmuch.devicemanager.ui.theme.ErrorRed
import kotlinx.coroutines.delay

/**
 *@author: Chen
 *@createTime: 2021/9/13 15:19
 *@description:
 **/
@Composable
fun LoadingScene(navController: NavController = LocalNavController.current) {
    val cxt = LocalContext.current
    val connectivityManager =
        cxt.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var networkConnected by remember {
        mutableStateOf(false)
    }
    var showConfigDialog by remember {
        mutableStateOf(false)
    }
    val viewModel = hiltViewModel<LoadingViewModel>()
    val deviceID by viewModel.deviceID.collectAsState()
    val mqttStatus by viewModel.mqttState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CorrectBlue),
        contentAlignment = Alignment.Center
    ) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        Column(
            Modifier
                .padding(vertical = 72.dp, horizontal = 24.dp)
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.text_device_info),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showConfigDialog = !showConfigDialog },
                color = CorrectBlue, style = MaterialTheme.typography.h6,
            )
            Column(Modifier.padding(bottom = 24.dp)) {
                InfoDetail(title = stringResource(id = R.string.text_device_id), content = deviceID)
                InfoDetail(
                    title = stringResource(id = R.string.text_resolution),
                    content = "$width*$height"
                )
                InfoDetail(
                    title = stringResource(id = R.string.text_wifi_status),
                    content = if (networkConnected) {
                        stringResource(R.string.text_connected)
                    } else {
                        stringResource(R.string.text_disconnect)
                    },
                    contentColor = if (networkConnected) CorrectBlue else ErrorRed
                )
                InfoDetail(
                    title = stringResource(id = R.string.text_connection_status),
                    content = if (mqttStatus == MQTTStatus.CONNECT_SUCCESS) {
                        stringResource(R.string.text_connected)
                    } else {
                        stringResource(R.string.text_disconnect)
                    },
                    contentColor = if (mqttStatus == MQTTStatus.CONNECT_SUCCESS) CorrectBlue else ErrorRed,
                )
            }
        }
    }

    if (showConfigDialog) {
        AppConfigDialog(viewModel = viewModel) {
            showConfigDialog = false
        }
    }
    LaunchedEffect(key1 = showConfigDialog) {
        delay(viewModel.autoJumpTime * 1000L)
        if (!showConfigDialog) {
            navController.navigate(Scene.Home.id) {
                popUpTo(Scene.Loading.id) { inclusive = true }
            }
        }
    }
    DisposableEffect(key1 = Unit) {
        val networkCallbacks = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                networkConnected = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                networkConnected = false
            }
        }
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallbacks
        )
        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallbacks)
        }
    }
}

@Composable
fun InfoDetail(title: String, content: String, contentColor: Color = Color.Unspecified) {
    val style = MaterialTheme.typography.body1
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = style.copy(color = style.color.copy(alpha = 0.8f)), textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = content,
            color = contentColor,
            modifier = Modifier.weight(1f),
            style = style.copy(color = style.color.copy(alpha = 0.8f))
        )
    }
}