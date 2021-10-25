package com.iknowmuch.devicemanager.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.blankj.utilcode.util.AppUtils
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.mqtt.MQTTStatus
import com.iknowmuch.devicemanager.ui.LocalInsetsController
import com.iknowmuch.devicemanager.ui.scene.loading.InfoDetail
import com.iknowmuch.devicemanager.ui.scene.loading.LoadingViewModel
import com.iknowmuch.devicemanager.ui.theme.CorrectBlue
import com.iknowmuch.devicemanager.ui.theme.ErrorRed

/**
 *@author: Chen
 *@createTime: 2021/9/14 17:30
 *@description:
 **/
@ExperimentalComposeUiApi
@Composable
fun AppGlobalInfoDialog(
    viewModel: LoadingViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onCloseApp: () -> Unit
) {
    val cxt = LocalContext.current
    val deviceID by viewModel.deviceID.collectAsState()
    val mqttStatus by viewModel.mqttState.collectAsState()
    var autoJumpTime by remember {
        mutableStateOf(viewModel.autoJumpTime.toString())
    }
    var chargingTime by remember {
        mutableStateOf(viewModel.chargingTime.toString())
    }
    var serialPortPath by remember {
        mutableStateOf(viewModel.serialPortPath)
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val insetsController = LocalInsetsController.current
        DisposableEffect(key1 = Unit) {
            onDispose {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoDetail(title = stringResource(id = R.string.text_device_id), content = deviceID)
            InfoDetail(
                title = stringResource(id = R.string.text_software_version),
                content = AppUtils.getAppVersionName()
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
            Spacer(modifier = Modifier.height(16.dp))
            //自动转跳设置
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.body1
                Text(
                    text = stringResource(id = R.string.text_auto_jump_time),
                    modifier = Modifier.weight(1f),
                    style = style.copy(color = style.color.copy(alpha = 0.8f)),
                    textAlign = TextAlign.End
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    BasicTextField(
                        textStyle = MaterialTheme.typography.body2,
                        value = autoJumpTime, onValueChange = {
                            autoJumpTime = it
                        }, modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                            .background(
                                MaterialTheme.colors.background,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(BorderStroke(1.dp, Color.LightGray))
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        try {
                            viewModel.saveAutoJumpTime(autoJumpTime.toInt())
                            Toast.makeText(cxt, "保存成功", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(cxt, "格式有误,只能输入数字", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = stringResource(id = R.string.text_save))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.body1
                Text(
                    text = stringResource(id = R.string.text_device_charging_time),
                    modifier = Modifier.weight(1f),
                    style = style.copy(color = style.color.copy(alpha = 0.8f)),
                    textAlign = TextAlign.End
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    BasicTextField(
                        textStyle = MaterialTheme.typography.body2,
                        value = chargingTime, onValueChange = {
                            chargingTime = it
                        }, modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                            .background(
                                MaterialTheme.colors.background,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(BorderStroke(1.dp, Color.LightGray))
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        try {
                            viewModel.saveChargingTime(chargingTime.toFloat())
                            Toast.makeText(cxt, "保存成功", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(cxt, "格式有误,只能输入数字", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = stringResource(id = R.string.text_save))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.body1
                Text(
                    text = stringResource(id = R.string.text_serial_port_path),
                    modifier = Modifier.weight(1f),
                    style = style.copy(color = style.color.copy(alpha = 0.8f)),
                    textAlign = TextAlign.End
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    BasicTextField(
                        textStyle = MaterialTheme.typography.body2,
                        value = serialPortPath, onValueChange = {
                            serialPortPath = it
                        }, modifier = Modifier
                            .width(160.dp)
                            .height(40.dp)
                            .background(
                                MaterialTheme.colors.background,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(BorderStroke(1.dp, Color.LightGray))
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        viewModel.saveSerialPortPath(serialPortPath)
                        Toast.makeText(cxt, "保存成功", Toast.LENGTH_SHORT).show()

                    }) {
                        Text(text = stringResource(id = R.string.text_save))
                    }
                }
            }
            TextButton(modifier = Modifier.padding(vertical = 8.dp), onClick = { onCloseApp() }) {
                Text(text = stringResource(id = R.string.text_close_app))
            }
        }
    }
}