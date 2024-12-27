package com.chatty.compose.ui.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chatty.compose.ui.theme.green

fun Context.hideIME() {
    (getSystemService(ComponentActivity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow((this as Activity).currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
fun Context.vibrate(time: Long) {
    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        .vibrate(time)
}
fun Context.hasCameraFlash(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
}
fun Modifier.drawLoginStateRing(isOnline: Boolean) = this.then(
    object : DrawModifier {
        override fun ContentDrawScope.draw() {
            val circleRadius = 20.dp.toPx()
            // DrawModifier这里默认是按照z轴往上画的，这里逻辑先绘制原本的内容，再追加两个圈
            drawContent()
            drawCircle(
                color = Color.White,
                radius = circleRadius,
                center = Offset(drawContext.size.width - circleRadius , drawContext.size.height - circleRadius)
            )
            drawCircle(
                color = if (isOnline) Color.Green else Color.Red,
                radius = circleRadius * 4 / 5,
                center = Offset(drawContext.size.width - circleRadius, drawContext.size.height - circleRadius)
            )
        }
    }
)

fun Modifier.customBorder(
    color: Color=green,
    width: Dp = 1.dp,
    cornerRadius: Dp = 0.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = width.toPx()
        val halfStrokeWidth = strokeWidth / 2
        drawRoundRect(
            color = color,
            size = size.copy(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth
            ),
            topLeft = Offset(halfStrokeWidth, halfStrokeWidth),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
            style = Stroke(strokeWidth)
        )
    }
)