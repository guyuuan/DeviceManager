package com.iknowmuch.devicemanager.ui.scene.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.iknowmuch.devicemanager.R
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.ui.LocalNavController
import com.iknowmuch.devicemanager.ui.theme.BatteryColor
import com.iknowmuch.devicemanager.ui.theme.BlueBrush
import com.iknowmuch.devicemanager.ui.theme.DefaultBlackTextColor
import com.iknowmuch.devicemanager.ui.theme.ErrorRed
import com.iknowmuch.devicemanager.ui.theme.GreenBrush
import com.iknowmuch.devicemanager.ui.theme.ThemeBlue
import com.iknowmuch.devicemanager.utils.drawColorShadow

/**
 *@author: Chen
 *@createTime: 2021/9/13 15:19
 *@description:
 **/
@ExperimentalFoundationApi
@Composable
fun HomeScene(navController: NavController = LocalNavController.current) {
    val viewModel = hiltViewModel<HomeViewModel>()
    Box(
        Modifier
            .fillMaxSize()
    ) {
        TopBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                viewModel = viewModel, modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                navController
            )
            UsingInstructionsCard(
                Modifier
                    .padding(horizontal = 32.dp)
                    .padding(top = 32.dp, bottom = 18.dp)
                    .fillMaxWidth()
                    .height(316.dp)
            )
            CabinetDoorList(
                data = viewModel.cabinetDoorList,
                Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .weight(1f)
            )
            BottomButton(
                viewModel = viewModel,
                Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 124.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun UsingInstructionsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, elevation = 2.dp
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "如何使用",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.ExtraBold,
                color = DefaultBlackTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            UsingInstructionItem(text = "如何借:点击底部的“借”按钮——用微信扫一扫——进入小程序")
            Spacer(modifier = Modifier.height(4.dp))
            UsingInstructionItem(text = "如何还:点击底部的“还”按钮——将探头编码对准摄像头——将设备放入柜中，并插好电源——关上柜门")
            Spacer(modifier = Modifier.height(4.dp))
            UsingInstructionItem(
                text = "没有设备:(1）点击右上角“更多”，查看其它智能柜是否有设备" +
                        "(2）所有的智能柜都" +
                        "没有设备，可以打开小程序——点击“我的”——点击“预约”"
            )
            Spacer(modifier = Modifier.height(4.dp))
            UsingInstructionItem(text = "联系管理员:打开小程序——点击“我的”——联系管理员")
        }
    }
}

@Composable
fun UsingInstructionItem(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(id = R.drawable.ic_rectangle),
            contentDescription = null,
            Modifier.size(29.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.body2, color = DefaultBlackTextColor)
    }
}

@Composable
fun BottomButton(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(204.dp)
                .clickable { }
                .background(
                    brush = GreenBrush, shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.text_borrow),
                style = MaterialTheme.typography.h4,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.width(28.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(204.dp)
                .clickable { }
                .background(brush = BlueBrush, shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.text_return),
                style = MaterialTheme.typography.h4,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun CabinetDoorList(data: List<CabinetDoor>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(cells = GridCells.Fixed(2), modifier = modifier) {
        itemsIndexed(data) { _, item ->
            CabinetDoorItem(
                data = item, modifier = Modifier
                    .drawColorShadow(
                        color = ThemeBlue,
                        padding = 14.dp,
                        alpha = 0.25f,
                        shadowSize = 2.dp,
                        offsetY = 8.dp
                    )
                    .padding(14.dp)
                    .fillMaxWidth()
                    .aspectRatio(494 / 273f)
            )
        }
    }
}

@Composable
fun CabinetDoorItem(data: CabinetDoor, modifier: Modifier) {
    val boxModifier = when (data.status) {
        CabinetDoor.Status.Idle, CabinetDoor.Status.Booked -> {
            Modifier.background(
                brush = GreenBrush, shape = MaterialTheme.shapes.medium
            )
        }
        CabinetDoor.Status.Empty -> {
            Modifier.background(
                brush = BlueBrush, shape = MaterialTheme.shapes.medium
            )
        }
        CabinetDoor.Status.Charging,
        CabinetDoor.Status.Error,
        CabinetDoor.Status.Fault -> {
            Modifier.background(
                color = MaterialTheme.colors.background,
                shape = MaterialTheme.shapes.medium
            )
        }
    }
    val spacerColor: Color
    CompositionLocalProvider(
        LocalContentColor provides when (data.status) {
            CabinetDoor.Status.Charging,
            CabinetDoor.Status.Error,
            CabinetDoor.Status.Fault -> {
                spacerColor = Color(0xFFE7E7E7)
                DefaultBlackTextColor
            }
            else -> {
                spacerColor = Color(0x5EFFFFFF)
                Color.White
            }
        }
    ) {
        Box(
            modifier = modifier then boxModifier
        ) {

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val leftColor: Color
                    val rightColor: Color
                    when (data.status) {
                        CabinetDoor.Status.Charging,
                        CabinetDoor.Status.Error,
                        CabinetDoor.Status.Fault -> {
                            leftColor = ThemeBlue
                            rightColor = if (data.status != CabinetDoor.Status.Charging) {
                                ErrorRed
                            } else {
                                leftColor
                            }
                        }
                        CabinetDoor.Status.Booked -> {
                            leftColor = Color.White
                            rightColor = ThemeBlue
                        }
                        else -> {
                            leftColor = Color.White
                            rightColor = Color.White
                        }
                    }
                    Text(
                        text = stringResource(R.string.text_cabinet_door_number).format(
                            data.id
                        ),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.h6,
                        color = leftColor
                    )
                    Text(
                        text = stringResource(data.status.id),
                        style = MaterialTheme.typography.body1, color = rightColor
                    )
                }

                Spacer(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 9.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(color = spacerColor)
                )
                Text(
                    text = if (data.deviceCode != null) stringResource(R.string.text_device_code).format(
                        data.deviceCode
                    ) else stringResource(
                        id = R.string.text_empty_device_code
                    ),
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (data.availableTime != null) stringResource(R.string.text_available_time).format(
                        data.availableTime
                    ) else
                        stringResource(id = R.string.text_empty_available_time),
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BatteryIcon(data = data.devicePower, data.status)
                    Text(text = "${data.devicePower}%", style = MaterialTheme.typography.body2)
                }
            }
        }

    }
}

@Composable
fun BatteryIcon(data: Int, status: CabinetDoor.Status) {
    val percent = if (data != 100 && status == CabinetDoor.Status.Charging) {
        val infiniteTransition = rememberInfiniteTransition()
        val animPercent by infiniteTransition.animateFloat(
            initialValue = data / 100f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = ((1 - data / 100f) * 10000).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
        animPercent
    } else {
        data / 100f
    }
    Box(contentAlignment = Alignment.CenterStart) {
        Image(
            painter = painterResource(id = R.drawable.ic_battery_border),
            modifier = Modifier
                .padding(end = 20.dp)
                .size(width = 76.dp, height = 41.dp), contentDescription = null
        )
        Canvas(
            modifier = Modifier
                .padding(start = 6.dp)
                .size(width = 58.dp, height = 27.dp)
        ) {
            drawRoundRect(
                BatteryColor,
                size = size.copy(width = size.width * percent),
                cornerRadius = CornerRadius(2 / 58f),
            )
        }
    }
//        Image(
//            painter = painterResource(id = R.drawable.ic_power_full),
//            modifier = Modifier
//                .padding(end = 20.dp)
//                .size(width = 76.dp, height = 41.dp), contentDescription = null
//        )
}

@Composable
fun TopBackground() {
    val color = MaterialTheme.colors.primary
    val radius = with(LocalDensity.current) { 2460.dp.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(416.dp)
    ) {
        drawCircle(
            color = color,
            center = Offset(size.width / 2, size.height - radius),
            radius = radius
        )
    }
}

@Composable
fun TopBar(viewModel: HomeViewModel, modifier: Modifier = Modifier, navController: NavController) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        Row(modifier = modifier) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "智能柜1",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.ExtraBold)
                )
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
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.clickable(
//                    indication = null,
//                    interactionSource = remember { MutableInteractionSource() }) {
//                    navController.navigate(Scene.More.id)
//                }) {
//                Text(text = "更多", fontSize = 40.sp)
//                Spacer(modifier = Modifier.width(20.dp))
//                Image(
//                    painter = painterResource(id = R.drawable.ic_circle_right_arrow),
//                    contentDescription = null
//                )
//            }
        }
    }
}