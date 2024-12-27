package com.chatty.compose.screens.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.internal.R.attr.maxLines
import com.android.internal.R.attr.textStyle
import com.chatty.compose.R
import com.chatty.compose.ui.components.*
import com.chatty.compose.ui.theme.green
import com.chatty.compose.ui.utils.LocalNavController


@Preview
@Composable
fun Demo() {
    PersonalProfileEditor("设置性别")
}

@Composable
fun PersonalProfileEditor(attr: String) {
        val isQRCode = (attr == "qrcode")
        val isGender = (attr == "gender")
        val title = when (attr) {
            "age" -> "输入年龄"
            "phone" -> "输入电话号"
            "email" -> "输入电子邮箱"
            "gender" -> "选择性别"
            else -> "展示二维码"
        }
    val navController = LocalNavController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            start = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            center =  {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 20.dp) // 它是被置于Box中，Box给其align(Alignment.Center),在这个基础上还start偏移20.dp
                )
            },
            end =  {
                if (!isQRCode) {
                    Button(
                        // Optimize: 注意它的顺序是先走builder里面，也就是先清空栈再跳转，所以能这么玩达到跳转主页并且关掉drawer的目的（后续有想到好的方法再调整）
                        onClick = { navController.navigate(AppScreen.main){
                            popUpTo(AppScreen.main) { inclusive = true }
                        } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = green
                        ),
                    ) {
                        Text(text = "完成")
                    } 
                }
            },
            backgroundColor = MaterialTheme.colorScheme.background
        )
        when {
            isGender -> GenderSelector()
            isQRCode -> QRCodeDisplay()
            else -> ProfileInputField()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInputField() {
    var inputText by remember {
        mutableStateOf("")
    }
    val focusRequester = remember {
        FocusRequester()
    }
    OutlinedTextField(
        value = inputText,
        onValueChange = {
            inputText = it
        },
        // Optimize: 一定得好好体会这个padding，设置在宽/高前就是margin的效果，设置在宽/高后就是padding的效果；
        //  同时后续的content是在padding后的区域内，这个跟传统View是一致的（compose在默认没有设置宽高的情况下是wrapContent）
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(green)
            .padding(20.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            .focusRequester(focusRequester),
        maxLines = 1,
        textStyle = TextStyle(
            fontSize = 20.sp
        ),
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun QRCodeDisplay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Image(painter = painterResource(id = R.drawable.qrcode), contentDescription = "qr_code")
            HeightSpacer(value = 5.dp)
            Text(text = "使用扫一扫添加我", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun GenderSelector() {
    var selectMale by remember {
        mutableStateOf(false)
    }
    Column {
        CenterRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectMale = true
                }
                .padding(horizontal = 20.dp)
                .height(80.dp)
        ) {
            CenterRow {
                Icon(
                    painter = painterResource(id = R.drawable.male),
                    contentDescription = "male",
                    tint = Color.Blue
                )
                WidthSpacer(value = 6.dp)
                Text(
                    text = "男",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (selectMale) {
                Icon(
                    painter = painterResource(id = R.drawable.correct),
                    contentDescription = "correct",
                    tint = green
                )
            }
        }
        Divider()
        CenterRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectMale = false
                }
                .padding(horizontal = 20.dp)
                .height(80.dp)
        ) {
            CenterRow {
                Icon(
                    painter = painterResource(id = R.drawable.female),
                    contentDescription = "male",
                    tint = Color(0xffd93a7d)
                )
                WidthSpacer(value = 6.dp)
                Text(
                    text = "女",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (!selectMale) {
                Icon(
                    painter = painterResource(id = R.drawable.correct),
                    contentDescription = "correct",
                    tint = green
                )
            }
        }
    }
}
