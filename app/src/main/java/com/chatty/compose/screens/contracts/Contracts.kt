package com.chatty.compose.screens.contracts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chatty.compose.R
import com.chatty.compose.bean.UserProfileData
import com.chatty.compose.screens.home.mock.friends
import com.chatty.compose.ui.components.AppScreen
import com.chatty.compose.ui.components.CenterRow
import com.chatty.compose.ui.components.CircleShapeImage
import com.chatty.compose.ui.components.TopBar
import com.chatty.compose.ui.theme.chattyColors
import com.chatty.compose.ui.theme.green
import com.chatty.compose.ui.utils.Comparator
import com.chatty.compose.ui.utils.LocalNavController
import com.chatty.compose.ui.utils.searchLastElementIndex
import com.chatty.compose.ui.utils.vibrate
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.launch


data class AlphaState(
    val alpha: Char,
    var state: MutableState<Boolean>
)

fun fetchLatestFriendsList(): List<UserProfileData> {
    return friends
}

@Preview
@Composable
fun Contracts() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentSelectedAlphaIndex by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        ContractTopBar()
        val currentFriends = fetchLatestFriendsList()
        val sortedFriends = remember(currentFriends) {
            currentFriends.groupBy {
                val firstChar = Pinyin.toPinyin(it.nickname.first().toString(), "").first()
                if (!firstChar.isLetter()) {
                    '#'
                } else {
                    firstChar.uppercaseChar()
                }
            }.toSortedMap { a: Char, b: Char -> // Optimize: 这一层排序的对象是Map,针对的是对Key(Char)的排序
                // Optimize: 注意a、b比较的时候，返回的是>0的话表示a（前者）排在后面，返回<0的话表示a排在前面
                when {
                    a == b -> 0
                    a == '#' -> 1
                    b == '#' -> -1// a排在前面，换句话说不就是b排在后面
                    else -> a.compareTo(b)
                }
            }.apply {
                // 这里排序是对value(List)内部按照字母排序，这里不是直接对List对象排序
                // 而是采用put覆盖的形式把排序好的value放回去（巧妙的利用copy思想，能避免多线程环境的并发问题）
                for ((k, v) in entries) {
                    put(k, v.sortedWith { a, b ->
                        a.nickname.compareTo(b.nickname)
                    })
                }
            }
        }
        val preSumIndexToStateMap = remember(sortedFriends) { mutableMapOf<Int, AlphaState>() }
        val alphaCountPreSumList = remember(sortedFriends) {
            var currentSum = 0
            var index = 0
            val result = mutableListOf<Int>()
            for ((alpha, friendList) in sortedFriends) {
                preSumIndexToStateMap[index] = AlphaState(alpha, mutableStateOf(false))
                result.add(currentSum)
                currentSum += (friendList.size + 1)
                index++
            }
            result
        }
        // alphaCountPreSumList: [0, 3, 8, 10, 12, 14, 16, 19, 22, 25, 29]
        println("alphaCountPreSumList: $alphaCountPreSumList")
        // preSumIndexToStateMap: {
        // 0=AlphaState(alpha=A, state=MutableState(value=false)@16114984),
        // 1=AlphaState(alpha=B, state=MutableState(value=false)@158659137),
        // 2=AlphaState(alpha=H, state=MutableState(value=false)@132195302),
        // 3=AlphaState(alpha=J, state=MutableState(value=false)@229674279),
        // 4=AlphaState(alpha=L, state=MutableState(value=false)@39332820),
        // 5=AlphaState(alpha=M, state=MutableState(value=false)@115175805),
        // 6=AlphaState(alpha=N, state=MutableState(value=false)@143366258),
        // 7=AlphaState(alpha=S, state=MutableState(value=false)@101635267),
        // 8=AlphaState(alpha=T, state=MutableState(value=false)@183906624),
        // 9=AlphaState(alpha=X, state=MutableState(value=false)@89434233),
        // 10=AlphaState(alpha=#, state=MutableState(value=false)@116836798)})
        println("preSumIndexToStateMap: $preSumIndexToStateMap")
        val lazyListState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyListState
            ) {
                sortedFriends.forEach {
                    item {
                        Text(
                            text = it.key.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.chattyColors.backgroundColor)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.W700,
                            color = Color(0xFF0079D3)
                        )
                    }
                    itemsIndexed(items = it.value) { _, user ->
                        FriendItem(user.avatarRes, user.nickname, user.motto) {
                            navController.navigate("${AppScreen.conversation}/${user.uid}")
                        }
                    }
                }
            }
            Box(Modifier.fillMaxSize()/*.background(green.copy(alpha = .5f))*/, contentAlignment = Alignment.CenterEnd) {
                AlphaGuildBar(preSumIndexToStateMap.values) { selectIndex ->
                    scope.launch {
                        lazyListState.scrollToItem(alphaCountPreSumList[selectIndex])
                        val newestAlphaIndex =
                            alphaCountPreSumList.searchLastElementIndex(object : Comparator<Int> {
                                override fun compare(target: Int): Boolean {
                                    // 上述LazyColumn先滚动到对应位置后，根据firstVisibleItemIndex计算出来的newestAlphaIndex当然与selectIndex相等
                                    return target <= lazyListState.firstVisibleItemIndex
                                }
                            })
                        // Optimize: 右侧bar点击到末尾‘#’，newestAlphaIndex: 7 , selectIndex: 10
                        /**
                         * 不是常态情况下：比如点击bar的最后一个‘#’，此时的selectIndex=10,但是lazyColumn计算出的newestAlphaIndex=7，最终bar显示的应该是newestAlphaIndex值
                         * 典型的上层来决定下层的UI显示，因为下层有很多信息没有结合到（比如就算滚动到末尾，第一个显示的是对应的是‘S’,右侧bar当前只能显示到‘S’啰）
                         */
                        println("newestAlphaIndex: $newestAlphaIndex , selectIndex: $selectIndex")
                        currentSelectedAlphaIndex = newestAlphaIndex
                        /**
                         * 默认情况下：bar与列表的index是可以联动对应上的给个震动（发起方：右侧滑竿的操作）
                         * 震动? newestAlphaIndex == selectIndex=0
                         * 震动? newestAlphaIndex == selectIndex=1
                         * 震动? newestAlphaIndex == selectIndex=4
                         * 震动? newestAlphaIndex == selectIndex=3
                         */
                        if (newestAlphaIndex == selectIndex) { // 没有出界才震动
                            println("震动? newestAlphaIndex == selectIndex=$newestAlphaIndex")
                            context.vibrate(50)
                        }
                    }
                }
            }
        }
        val afterFirstVisibleItem by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

        // 根据LazyColumn的滚动位置，计算当前应该选中的curSelectedIndex（后面的逻辑会根据这个状态值的变化，触发右侧AlphaGuildBar的UI更新）
        LaunchedEffect(afterFirstVisibleItem) {
            currentSelectedAlphaIndex =
                alphaCountPreSumList.searchLastElementIndex(object : Comparator<Int> {
                    override fun compare(target: Int): Boolean {
                        return target <= lazyListState.firstVisibleItemIndex
                    }
                })
        }

        // 在curSelectedIndex变化时，修改对应的AlphaState的状态从而触发AlphaGuildBar的UI更新
        LaunchedEffect(currentSelectedAlphaIndex) {
            val alphaState = preSumIndexToStateMap[currentSelectedAlphaIndex]!!
            preSumIndexToStateMap.values.forEach {
                it.state.value = false
            }
            alphaState.state.value = true
        }
    }
}

@Composable
fun ContractTopBar() {
    val navController = LocalNavController.current
    TopBar(
        center = {
            Text("通讯录", color = MaterialTheme.chattyColors.textColor)
        },
        end = {
            IconButton(
                onClick = {
                    navController.navigate(AppScreen.addFriends)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_friend),
                    "add_friends",
                    tint = MaterialTheme.chattyColors.iconColor
                )
            }
        },
        backgroundColor = MaterialTheme.chattyColors.backgroundColor
    )
}

@Composable
fun FriendItem(
    avatarRes: Int,
    friendName: String,
    motto: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.chattyColors.backgroundColor
    ) {
        CenterRow(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            CircleShapeImage(60.dp, painter = painterResource(id = avatarRes))
            Spacer(Modifier.padding(horizontal = 10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friendName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.chattyColors.textColor
                )
                Spacer(Modifier.padding(vertical = 3.dp))
                Text(
                    text = motto,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.chattyColors.textColor
                )
            }
        }
    }
}

@Composable
fun AlphaGuildBar(alphaStates: MutableCollection<AlphaState>, updateSelectIndex: (Int) -> Unit) {
    val alphaItemHeight = 28.dp
    val density = LocalDensity.current
    var currentIndex by remember { mutableStateOf(-1) }
    var displayBox by remember { mutableStateOf(false) }
    // offsetY的单位是dp哟，它的值是根据上述alphaItemHeight（单位dp）变动而来的
    var offsetY by remember { mutableStateOf(0f) }
    Row {
        if (displayBox) HoverBox(alphaStates, currentIndex, offsetY)
        Column(
            modifier = Modifier.pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        displayBox = false
                    },
                    onDragStart = {
                        displayBox = true
                    }
                ) { change, _ ->
                    with(density) {
                        // Optimize: 注意change.position.y是相对于父组件的位置，并非增量；也就是在拖动过程中，这个值记录着距离父容器的位置
                        /**
                         * 稍微在A的末尾拖动一点点：
                         *  System.out               I  change.position.y: 55.555542
                         *  System.out               I  change.position.y: 57.333313
                         */
                        // println("change.position.y: ${change.position.y}")
                        currentIndex = (change.position.y / alphaItemHeight.toPx()).toInt()
                            .coerceIn(0, alphaStates.size - 1)
                    }
                }
            }
        ) {
            alphaStates.forEachIndexed { index: Int, alphaState: AlphaState ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (alphaState.state.value) green else Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    currentIndex = index
                                    println("点击了: currentIndex=$currentIndex")
                                }
                            )
                        }
                ) {
                    Text(
                        text = alphaState.alpha.toString(),
                        color = if (alphaState.state.value || !MaterialTheme.chattyColors.isLight) Color.White else Color.Black
                    )
                }
            }
        }
    }
    /**
     * 上述的currentIndex并没有在Composable内部做限制，而是把这个逻辑交给上层处理也就是采用接口回调的形式抛上去（很符合MVI的状态上移）
     * 所以表现形式是：该滑动bar的最后一个字母显示与否是有上层Composable决定的
     */
    LaunchedEffect(currentIndex) {
        if (currentIndex == -1) {
            return@LaunchedEffect
        }
        updateSelectIndex(currentIndex)
        offsetY = currentIndex * alphaItemHeight.value - 15f
    }
}

@Composable
fun HoverBox(
    alphaStates: MutableCollection<AlphaState>,
    currentIndex: Int,
    offsetY: Float
) {
    Box(
        modifier = Modifier
            .padding(end = 20.dp)
            .offset(y = offsetY.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(60.dp)) {
            drawCircle(
                color = Color.Gray
            )
            val trianglePath = Path().let {
                it.moveTo(this.size.width * 1.2f, this.size.height / 2)
                it.lineTo(this.size.width * .9f, this.size.height * .2f)
                it.lineTo(this.size.width * .9f, this.size.height * .8f)
                it.close()
                it
            }
            drawPath(trianglePath, color = Color.Gray)
        }
        Text(
            text = alphaStates.elementAt(currentIndex).alpha.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}
