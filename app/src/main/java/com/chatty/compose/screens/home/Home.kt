package com.chatty.compose.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chatty.compose.screens.home.mock.displayMessages
import com.chatty.compose.ui.theme.chattyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(drawerState: DrawerState) {
    Scaffold(
        topBar = {
            HomeTopBar(drawerState)
            // Box(
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .height(200.dp)
            //         .background(MaterialTheme.colorScheme.primary)
            //         .statusBarsPadding()
            // ){
            //     Text(
            //         text = "Home",
            //         style = MaterialTheme.typography.titleMedium
            //     )
            // }
        }
    ) { innerPadding ->
        // Scaffold--innerPadding: PaddingValues(start=0.0.dp, top=88.0.dp, end=0.0.dp, bottom=0.0.dp)
        println("Scaffold--innerPadding: $innerPadding")
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.chattyColors.backgroundColor)
        ) {
            itemsIndexed(
                displayMessages, key = { _, item ->
                    item.mid
                }
            ) { _, item ->
                FriendMessageItem(item.userProfile, item.lastMsg, item.unreadCount)
            }
        }
    }
}
