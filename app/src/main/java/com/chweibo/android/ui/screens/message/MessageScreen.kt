package com.chweibo.android.ui.screens.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chweibo.android.ui.theme.WeiboOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToComments: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "消息",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 消息类型入口
            item {
                MessageTypeGrid(
                    onAtMeClick = { /* @我的 */ },
                    onCommentClick = { /* 评论 */ },
                    onLikeClick = { /* 赞 */ },
                    onMessageClick = { /* 私信 */ }
                )
            }

            // 消息列表
            items(10) { index ->
                MessageListItem(
                    title = "消息通知 ${index + 1}",
                    content = "这是一条消息内容的预览...",
                    time = "10:${index}0",
                    unreadCount = if (index < 3) index + 1 else 0
                )
            }
        }
    }
}

@Composable
fun MessageTypeGrid(
    onAtMeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MessageTypeItem(
            icon = Icons.Outlined.AlternateEmail,
            title = "@我的",
            badgeCount = 2,
            onClick = onAtMeClick
        )
        MessageTypeItem(
            icon = Icons.Outlined.ChatBubbleOutline,
            title = "评论",
            badgeCount = 5,
            onClick = onCommentClick
        )
        MessageTypeItem(
            icon = Icons.Outlined.FavoriteBorder,
            title = "赞",
            badgeCount = 12,
            onClick = onLikeClick
        )
        MessageTypeItem(
            icon = Icons.Outlined.Email,
            title = "私信",
            badgeCount = 0,
            onClick = onMessageClick
        )
    }
}

@Composable
fun MessageTypeItem(
    icon: ImageVector,
    title: String,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = WeiboOrange.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WeiboOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 未读徽章
            if (badgeCount > 0) {
                Badge(
                    containerColor = Color.Red,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MessageListItem(
    title: String,
    content: String,
    time: String,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 打开消息详情 */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
            color = WeiboOrange.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = WeiboOrange
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 内容
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = Color.Red,
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
