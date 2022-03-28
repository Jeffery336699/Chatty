package com.chatty.compose.screens.chatty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chatty.compose.R
import com.chatty.compose.ui.components.CircleShapeImage

@Composable
fun MyTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chatty", modifier = Modifier)
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                CircleShapeImage(size = 32.dp, painter = painterResource(id = R.drawable.ava4))
            }
        },
        actions = {
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(Icons.Rounded.Search, null)
            }
        },
        backgroundColor = Color.White,
        elevation = 12.dp
    )
}
