package com.iknowmuch.devicemanager.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.blankj.utilcode.util.AppUtils
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.ui.scene.loading.LoadingViewModel

/**
 *@author: Chen
 *@createTime: 2021/9/14 11:22
 *@description:
 **/
@Composable
fun AppConfigDialog(viewModel: LoadingViewModel, onDismissRequest: () -> Unit) {
    var httpServer by remember {
        mutableStateOf(viewModel.httpServer)
    }
    var mqttServer by remember {
        mutableStateOf(viewModel.mqttServer)
    }
    var keepLive by remember {
        mutableStateOf(viewModel.keepLive)
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 48.dp)
                .padding(top = 32.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConfigInputItem(
                title = stringResource(R.string.text_http_server),
                input = httpServer,
                onValueChange = {
                    httpServer = it
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ConfigInputItem(
                title = stringResource(R.string.text_amq_server),
                input = mqttServer,
                onValueChange = {
                    mqttServer = it
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.body1
                Text(text = "开启保活", style = style.copy(color = style.color.copy(alpha = 0.8f)))
                Spacer(modifier = Modifier.width(26.dp))
                Switch(checked = keepLive, onCheckedChange = { keepLive = it })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { onDismissRequest() },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(text = stringResource(R.string.text_cancel))
                }
                TextButton(onClick = {
                    viewModel.saveAppConfig(httpServer, mqttServer, keepLive)
                    onDismissRequest()
                    AppUtils.relaunchApp(true)
                }, contentPadding = PaddingValues(12.dp)) {
                    Text(text = stringResource(R.string.text_confirm))
                }
            }
        }
    }
}

@Composable
fun ConfigInputItem(
    title: String,
    input: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val style = MaterialTheme.typography.body1
        Text(text = title, style = style.copy(color = style.color.copy(alpha = 0.8f)))
        Spacer(modifier = Modifier.width(16.dp))
        BasicTextField(
            textStyle = MaterialTheme.typography.body2,
            value = input, onValueChange = onValueChange, modifier = Modifier
                .weight(1f)
                .height(45.dp)
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(4.dp))
                .border(BorderStroke(2.dp, Color.LightGray))
                .padding(4.dp)
        )
    }
}