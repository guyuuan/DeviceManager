package com.iknowmuch.devicemanager.ui.scene.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.ui.LocalNavController

/**
 *@author: Chen
 *@createTime: 2021/9/13 15:19
 *@description:
 **/
@Composable
fun HomeScene(navController: NavController = LocalNavController.current) {
    val viewModel = hiltViewModel<HomeViewModel>()
    Surface(color = MaterialTheme.colors.primary) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.padding(32.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "智能柜1", style = MaterialTheme.typography.h5)
                    Text(
                        text = stringResource(R.string.text_device_id_format).format(viewModel.deviceID),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = null
                        )
                        Text(
                            text = "西湖区A医院1号楼一楼东北角体检科对面",
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "更多", fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_circle_right_arrow),
                        contentDescription = null
                    )
                }
            }
        }
    }
}