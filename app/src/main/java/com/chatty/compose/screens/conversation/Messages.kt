package com.chatty.compose.screens.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chatty.compose.R
import com.chatty.compose.ui.components.AppScreen
import com.chatty.compose.ui.theme.chattyColors
import com.chatty.compose.ui.utils.customBorder


@Composable
fun Messages(
    messages: List<Message>,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val conversationUser = LocalConversationUser.current
    // 外层是一个Column的情况下，这里加个Box我感觉是一种防御性编程，避免出现一些不可预知的问题（eg滑动冲突）；尽量把未知情况控制在“自身范围”
    Box(modifier = modifier) {
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            // Add content padding so that the content can be scrolled (y-axis)
            // below the status bar + app bar
            // 在statusBars的基础上再额外加个90dp的topPadding,为的是内容距离顶部有一定的距离，美观些
            contentPadding =
            WindowInsets.statusBars.add(WindowInsets(top = 90.dp)).asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            for (index in messages.indices) {
                val preTime = messages.getOrNull(index - 1)?.timestamp
                val nextTime = messages.getOrNull(index + 1)?.timestamp
                val content = messages[index]
                val isFirstMessageByTime = preTime != content.timestamp
                val isLastMessageByTime = nextTime != content.timestamp
                /**
                 * 消息[index=0],isFirstMessageByTime: true, isLastMessageByTime: true
                 * 消息[index=1],isFirstMessageByTime: true, isLastMessageByTime: true
                 * 消息[index=2],isFirstMessageByTime: true, isLastMessageByTime: false
                 * 消息[index=3],isFirstMessageByTime: false, isLastMessageByTime: true
                 * 消息[index=4],isFirstMessageByTime: true, isLastMessageByTime: true
                 * 消息[index=5],isFirstMessageByTime: true, isLastMessageByTime: true
                 */
                println("消息[index=$index],isFirstMessageByTime: $isFirstMessageByTime, isLastMessageByTime: $isLastMessageByTime")
                if (index == messages.size - 1) {
                    item {
                        DayHeader("8月20日")
                    }
                } else if (index == 2) {
                    item {
                        DayHeader("今天")
                    }
                }

                item {
                    Message(
                        onAuthorClick = { navigateToProfile("${AppScreen.conversation}/${conversationUser.uid}") },
                        msg = content,
                        isUserMe = content.isUserMe,
                        isFirstMessageByAuthor = isFirstMessageByTime,
                        isLastMessageByAuthor = isLastMessageByTime
                    )
                }
            }
        }

    }
}

@Composable
fun Message(
    onAuthorClick: () -> Unit,
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
) {
    val borderColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {
        if (isLastMessageByAuthor) {
            // Avatar
            Image(
                modifier = Modifier
                    .clickable(enabled = !isUserMe, onClick = onAuthorClick)
                    .padding(horizontal = 16.dp)
                    .size(42.dp)
                    .border(1.5.dp, borderColor, CircleShape)
                    // Optimize: 第二次再执行一次border的话，效果并不会覆盖前一个设置好的border，是在前一个border的基础上再加一层border，
                    //  而从表现上看是处于前一个border的“底层”，最终多图层叠加后是前一个在上面（理解成前者border优先级高，覆盖后者）
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .align(Alignment.Top),
                painter = painterResource(id = if (isUserMe) R.drawable.ava2 else LocalConversationUser.current.avatarRes),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        } else {
            // Space under avatar
            Spacer(modifier = Modifier.width(74.dp))
        }
        AuthorAndTextMessage(
            msg = msg,
            isUserMe = isUserMe,
            isFirstMessageByTime = isFirstMessageByAuthor,
            isLastMessageByTime = isLastMessageByAuthor,
            authorClicked = onAuthorClick,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)
        )
    }
}


@Composable
fun AuthorAndTextMessage(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByTime: Boolean,
    isLastMessageByTime: Boolean,
    authorClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier/*.customBorder()*/) {
        if (isLastMessageByTime) {
            AuthorNameTimestamp(msg)
        }
        ChatItemBubble(msg, isUserMe, authorClicked)
        if (isFirstMessageByTime) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun AuthorNameTimestamp(msg: Message) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = if (msg.isUserMe) stringResource(id = R.string.author_me/*R.string.author_me_lines*/) else LocalConversationUser.current.nickname,
            style = MaterialTheme.typography.titleMedium,
            // Optimize: 这里是更加精确的定位到最后一行的baseline，是一种底部基线对齐的方式并且再往下padding 8dp（marginBottom的意思）
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp), // 为了看效果最好把值设置大一些，比如58dp
            color = MaterialTheme.chattyColors.textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = msg.timestamp,
            /*text = """
                ${msg.timestamp}
                ${msg.timestamp}
                ${msg.timestamp}""".trimIndent()*/
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.chattyColors.conversationHintText),
            // Optimize: 同样也是应该以基线对齐，但是这里是以上一个Text的最后一行基线对齐（有种约束布局链的感觉）
            modifier = Modifier.alignBy(LastBaseline),
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)


@Composable
fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .wrapContentHeight()
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.chattyColors.disabledContent
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.chattyColors.disabledContent
    )
}

@Composable
fun ChatItemBubble(
    message: Message,
    isUserMe: Boolean,
    authorClicked: () -> Unit = {}
) {

    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.chattyColors.conversationBubbleBgMe
    } else {
        MaterialTheme.chattyColors.conversationBubbleBg
    }

    Column {
        Surface(
            color = backgroundBubbleColor,
            shape = ChatBubbleShape,
            shadowElevation = ConvasationBubbleElevation
        ) {
            ClickableMessage(
                message = message,
                isUserMe = isUserMe,
                authorClicked
            )
        }

        message.image?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = backgroundBubbleColor,
                shape = ChatBubbleShape,
                shadowElevation = ConvasationBubbleElevation
            ) {
                Image(
                    painter = painterResource(it),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp),
                    contentDescription = stringResource(id = R.string.attached_image)
                )
            }
        }
    }
}

@Composable
fun ClickableMessage(
    message: Message,
    isUserMe: Boolean,
    authorClicked: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val styledMessage = messageFormatter(
        text = String.format(message.content, LocalConversationUser.current.nickname),
        primary = isUserMe
    )

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isUserMe) MaterialTheme.chattyColors.conversationTextMe
            else MaterialTheme.chattyColors.conversationText
        ),
        modifier = Modifier.padding(16.dp),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    /** 这是上面的messageFormatter方法给你预整了两个带注解的标识，这里是直接拿着用 */
                    when (annotation.tag) {
                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                        SymbolAnnotationType.PERSON.name -> if (!isUserMe) authorClicked()
                        else -> Unit
                    }
                }
        }
    )
}

private val ConvasationBubbleElevation = 5.dp