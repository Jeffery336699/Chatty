package com.chatty.compose.screens.contracts

import android.Manifest
import android.app.Activity
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.chatty.compose.R
import com.chatty.compose.ui.components.AppScreen
import com.chatty.compose.ui.components.TopBar
import com.chatty.compose.ui.utils.LocalNavController
import com.chatty.compose.ui.utils.USER_CODE_PREFIX
import com.chatty.compose.ui.utils.hasCameraFlash
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.king.zxing.CaptureHelper
import com.king.zxing.OnCaptureCallback
import com.king.zxing.ViewfinderView
import com.king.zxing.camera.CameraConfigurationUtils


private fun setTorch(helper: CaptureHelper, on: Boolean) {
    val camera = helper.cameraManager.openCamera.camera
    val parameters = camera.parameters
    CameraConfigurationUtils.setTorch(parameters, on)
    camera.parameters = parameters
}

/**
 * 二维码扫描页面
 * 1. 推荐使用在线草料二维码生成测试 [草料](https://cli.im/text/other)
 * 2. 需要使用带固定前缀的二维码，例如 user://1007
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRCodeScanner() {
    val naviController = LocalNavController.current
    val context = LocalContext.current
    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    val hasTorch = remember { context.hasCameraFlash() }
    val surfaceView = remember { SurfaceView(context) }
    val viewfinderView = remember { ViewfinderView(context) }
    var isUsingFlashLight by remember { mutableStateOf(false) }
    val helper = remember {
        CaptureHelper(context as Activity, surfaceView, viewfinderView).apply {
            val captureCallback = OnCaptureCallback {
                // 待处理
                if (it.startsWith(USER_CODE_PREFIX)) {
                    val uid = it.removePrefix(USER_CODE_PREFIX)
                    naviController.navigate("${AppScreen.strangerProfile}/${uid}/二维码搜索")
                }
                // restartPreviewAndDecode()
                true
            }
            setOnCaptureCallback(captureCallback)
            playBeep(true)
            continuousScan(true)
            autoRestartPreviewAndDecode(false)
            onCreate()
            (context as? ComponentActivity)?.lifecycle?.addObserver(object :
                DefaultLifecycleObserver {

                override fun onPause(owner: LifecycleOwner) {
                    this@apply.onPause()
                }

                override fun onResume(owner: LifecycleOwner) {
                    this@apply.onResume()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    this@apply.onDestroy()
                }
            })
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { surfaceView },
            modifier = Modifier.fillMaxSize()
        )
        AndroidView(
            factory = { viewfinderView },
            modifier = Modifier.fillMaxSize()
        )
        if (hasTorch) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = if (isUsingFlashLight) R.drawable.ec_on else R.drawable.ec_off),
                    contentDescription = "flash",
                    modifier = Modifier
                        .padding(top = 150.dp)
                        .clickable {
                            isUsingFlashLight = !isUsingFlashLight
                            setTorch(helper, isUsingFlashLight)
                        }
                )
            }
        }
        QrCodeScanTopBar()
    }
    DisposableEffect(helper) {
        onDispose {
            helper.onDestroy()
        }
    }
    // 补充个动态权限申请
    if (!cameraPermissionState.status.isGranted) {
        SideEffect {
            cameraPermissionState.launchPermissionRequest()
        }
    }

}

@Composable
fun QrCodeScanTopBar() {
    val naviController = LocalNavController.current
    TopBar(
        start = {
            IconButton(onClick = {
                naviController.popBackStack()
            }) {
                Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
            }
        },
        center = {
            Text("扫一扫", color = Color.White, fontWeight = FontWeight.Bold)
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    )
}
