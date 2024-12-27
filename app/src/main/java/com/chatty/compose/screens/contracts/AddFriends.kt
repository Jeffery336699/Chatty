package com.chatty.compose.screens.contracts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatty.compose.R
import com.chatty.compose.bean.UserProfileData
import com.chatty.compose.screens.home.mock.friends
import com.chatty.compose.ui.components.*
import com.chatty.compose.ui.theme.chattyColors
import com.chatty.compose.ui.utils.LocalNavController
import com.chatty.compose.ui.utils.customBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max


class AddFriendsViewModel() {
    var isSearching by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var searchContent by mutableStateOf("")
    // 内部的数据状态、数据流、逻辑操作统一到viewModel层，单一数据源便于更好的管理（MVI）
    var displaySearchUsersFlow = MutableStateFlow<List<UserProfileData>>(listOf())
    suspend fun refreshFriendSearched() {
        delay(3000)
        val currentResult = friends.filter {
            it.nickname.lowercase(Locale.getDefault()).contains(searchContent,true)
        }.toMutableList()
        displaySearchUsersFlow.emit(currentResult)
        isLoading = false
    }

    fun clearSearchStatus() {
        displaySearchUsersFlow.tryEmit(emptyList())
        searchContent = ""
        isLoading = false
        isSearching = false
    }
}



@Composable
fun AddFriends(viewModel: AddFriendsViewModel) {
    val naviController = LocalNavController.current
    // 通过collectAsState()来获取Flow的最新值,同样是响应式的（只要是State包裹）
    val displaySearchUsers = viewModel.displaySearchUsersFlow.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.chattyColors.backgroundColor)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        AnimatedVisibility(visible = !viewModel.isSearching) {
            AddFriendTopBar()
        }
        HeightSpacer(value = 5.dp)
        SearchFriendBar(viewModel)
        HeightSpacer(value = 10.dp)
        if(!viewModel.isSearching) {
            AddFriendsOtherWay()
        } else {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.chattyColors.textColor)
            }
            LazyColumn {
                displaySearchUsers.value.forEach {
                    item(it.uid) {
                        FriendItem(avatarRes = it.avatarRes, friendName = it.nickname, motto = it.motto) {
                            naviController.navigate("${AppScreen.strangerProfile}/${it.uid}/用户名搜索")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddFriendTopBar() {
    val navController = LocalNavController.current
    TopBar(
        start = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(painter = painterResource(id = R.drawable.back), "add_friends", tint = MaterialTheme.chattyColors.iconColor)
            }
        },
        center =  {
            Text("添加联系人", color = MaterialTheme.chattyColors.textColor)
        },
        // 去除默认的背景色和阴影
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        contentPadding = AppBarDefaults.ContentPadding
    )
}

@Composable
fun SearchFriendBar(viewModel: AddFriendsViewModel) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    SubcomposeSearchFriendRow(
        modifier = Modifier.padding(horizontal = 10.dp),
        textField = {
            BasicTextField(
                value = viewModel.searchContent,
                onValueChange = {
                    viewModel.searchContent = it
                },
                modifier = Modifier
                    .height(50.dp)
                    .border(1.dp, MaterialTheme.chattyColors.textColor, RoundedCornerShape(5.dp))
                    .onFocusChanged {
                        // 默认刚进来什么都不操作的情况，仍会触发一次isFocused为false的事件
                        // 111111111  isFocused: false
                        println("111111111  isFocused: ${it.isFocused}")
                        viewModel.isSearching = it.isFocused
                        if (!viewModel.isSearching) {
                            viewModel.clearSearchStatus()
                        }
                    },
                textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.chattyColors.textColor),
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onSearch = {
                        scope.launch {
                            viewModel.isLoading = true
                            viewModel.refreshFriendSearched()
                        }
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                cursorBrush = SolidColor(MaterialTheme.chattyColors.textColor)
            ) { innerText ->
                CenterRow(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        contentAlignment = if (viewModel.isSearching) Alignment.CenterStart else Alignment.Center
                    ) {
                        if (!viewModel.isSearching) {
                            CenterRow {
                                Icon(
                                    painter = painterResource(id = R.drawable.search),
                                    contentDescription = null,
                                    tint = MaterialTheme.chattyColors.iconColor
                                )
                                WidthSpacer(value = 3.dp)
                                Text(
                                    text = "UID/用户名",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.LightGray
                                )
                            }
                        }
                        innerText()
                    }
                    if (viewModel.searchContent.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.searchContent = "" },
                        ) {
                            Icon(Icons.Filled.Close, null, tint = MaterialTheme.chattyColors.iconColor)
                        }
                    }
                }
            }
        },
        cancel = {
            if (viewModel.isSearching) {
                TextButton(onClick = {
                    viewModel.clearSearchStatus()
                    focusManager.clearFocus()
                }) {
                    Text(text = "取消", color = MaterialTheme.chattyColors.textColor)
                }
            }
        }
    )
}


@Composable
fun SubcomposeSearchFriendRow(modifier: Modifier, textField: @Composable () -> Unit, cancel: @Composable () -> Unit) {
    /**
     * SubcomposeLayout使用中仍要使用`subcompose`来获取Measurable,然后常规的measure、layout都是要的
     * 它太强大与灵活了，可以根据兄弟组件的测量情况来动态的调整自己的测量constraint(But性能有稍微逊色)
     */
    SubcomposeLayout(modifier) { constraints ->
        val cancelMeasureables = subcompose("cancel") { cancel() }
        var cancelPlaceable: Placeable? = null
        if (cancelMeasureables.isNotEmpty()) {
            cancelPlaceable = cancelMeasureables.first().measure(constraints = constraints)
        }
        val consumeWidth = cancelPlaceable?.width ?: 0
        /**
         * 从没有聚集（取消当前没有）到聚焦（取消存在）：
         *  22222  consumeWidth: 0 , constraints.maxWidth: 1028 , constraints.minWidth: 0
         *  22222  consumeWidth: 152 , constraints.maxWidth: 1028 , constraints.minWidth: 0
         */
        println("22222  consumeWidth: $consumeWidth , constraints.maxWidth: ${constraints.maxWidth} , constraints.minWidth: ${constraints.minWidth}")
        val textFieldMeasureables = subcompose("text_field") { textField() }.first()
        // Optimize: 这里是自定义一种TextField的weight=1的伸缩效果，即TextField占据剩余的空间
        val textFieldPlaceables = textFieldMeasureables.measure(
            constraints.copy(
                minWidth = constraints.maxWidth - consumeWidth,
                maxWidth = constraints.maxWidth - consumeWidth
            )
        )
        val width = constraints.maxWidth
        val height = max(cancelPlaceable?.height ?: 0, textFieldPlaceables.height)
        // Optimize: 最终还是得确定当前layout的宽高（此时的是自定义Row容器），类似ViewGroup的setMeasuredDimension确定好最终宽高，以及子View的摆放位置
        layout(width, height) {
            textFieldPlaceables.placeRelative(0, 0)
            cancelPlaceable?.placeRelative(textFieldPlaceables.width, 0)
        }
    }
}

@Composable
fun AddFriendsOtherWay() {
    Column() {
        val navController = LocalNavController.current
        AddFriendsOtherWayItem(
            R.drawable.qr_code,
            "扫一扫",
            "扫描二维码添加联系人"
        ) {
            navController.navigate(AppScreen.qr_scan)
        }
    }
}

@Composable
fun AddFriendsOtherWayItem(
    icon: Int,
    functionName: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.chattyColors.backgroundColor)
        .padding(horizontal = 10.dp)
        .clickable {
            onClick()
        }
    ){
        CenterRow(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                painter =  painterResource(id = icon),
                contentDescription = "qr_code",
                tint = MaterialTheme.chattyColors.iconColor,
                modifier = Modifier
                    .size(60.dp)
                    // .padding(12.dp)
            )
            WidthSpacer(value = 10.dp)
            Column{
                Text(
                    text = functionName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.chattyColors.textColor
                )
                HeightSpacer(3.dp)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.chattyColors.textColor
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.expand_right),
            contentDescription = "",
            tint = MaterialTheme.chattyColors.iconColor,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
