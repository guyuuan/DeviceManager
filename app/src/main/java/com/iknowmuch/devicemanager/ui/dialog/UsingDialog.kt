package com.iknowmuch.devicemanager.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.bean.ControllerResult

/**
 *@author: Chen
 *@createTime: 2021/10/22 15:54
 *@description:
 **/
@ExperimentalComposeUiApi
@Composable
fun UsingDialog(data: ControllerResult, onDismissRequest: () -> Unit) {
    if (!data.openState) return
    if (data.closeState) onDismissRequest()
    AutoCloseDialog(onDismissRequest = onDismissRequest) {
        AutoCloseColumn(
            showCountdown = false,
            time = 60,
            modifier = Modifier.fillMaxWidth(),
            onCountdownEnd = { onDismissRequest() }) {
            Column(
                Modifier
                    .padding(horizontal = 36.dp)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(if (data.status == 0) R.string.text_borrowing else R.string.text_returning),
                    modifier = Modifier
                        .padding(vertical = 30.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = stringResource(id = if (data.status == 0) R.string.text_borrow_tip else R.string.text_return_tip).format(
                        data.doorNo
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
        }
    }
}