package com.iknowmuch.devicemanager.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 *@author: Chen
 *@createTime: 2021/9/17 15:47
 *@description:
 **/
private val TransparentColor = Color(0x00FFFFFF).toArgb()
fun Modifier.drawColorShadow(
    color: Color,
    alpha: Float = 0.8F,
    padding:Dp=0.dp,
    shadowSize: Dp=0.dp,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = drawBehind {
    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = TransparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.copy(alpha = alpha).toArgb()
        )
        val realPadding = (padding - shadowSize).toPx()
        it.drawRoundRect(
            0f+realPadding,
            0f+realPadding,
            size.width-realPadding,
            size.height-realPadding,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}
