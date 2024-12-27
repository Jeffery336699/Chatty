package com.chatty.compose.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.chatty.compose.screens.home.Home
import com.chatty.compose.screens.contracts.Contracts
import com.chatty.compose.screens.drawer.PersonalProfile
import com.chatty.compose.screens.explorer.Explorer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AppScaffold() {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf(0) }

    ModalNavigationDrawer(
        drawerContent = {
            PersonalProfile()
        },
        drawerState = drawerState,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Scaffold(
            bottomBar = {
                MyBottomNavigationBar(
                    selectedScreen = selectedScreen,
                    onClick = {
                        scope.launch {
                            pagerState.scrollToPage(it)
                        }
                    }
                )
            }
        ) {
            // padding: PaddingValues(start=0.0.dp, top=0.0.dp, end=0.0.dp, bottom=80.0.dp)
            println("padding: $it")
            HorizontalPager(
                count = BottomScreen.values().size,
                state = pagerState,
                userScrollEnabled = false,
                contentPadding = it
            ) { page ->
                when (BottomScreen.values()[page]) {
                    BottomScreen.Message -> Home(drawerState)/*Text("Home", modifier = Modifier.fillMaxSize().padding(0.dp).background(
                        green), style = MaterialTheme.typography.headlineLarge,textAlign = TextAlign.Left)*/
                    BottomScreen.Contract -> Contracts()
                    BottomScreen.Explore -> Explorer()
                }
            }
        }
    }

    // Optimize: key为对象的话，在对象的引用不变的情况下，不会重新执行LaunchedEffect
    LaunchedEffect(pagerState) {
        println("LaunchedEffect: currentPage=${pagerState.currentPage}")
        snapshotFlow { pagerState.currentPage }.collect { page ->
            println("collect: currentPage=${pagerState.currentPage}")
            selectedScreen = page
        }
    }

    BackHandler(drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

}
