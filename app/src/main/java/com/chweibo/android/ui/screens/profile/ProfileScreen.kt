package com.chweibo.android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.User
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDrafts: () -> Unit,
    onNavigateToUserTimeline: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WeiboOrange,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 用户信息卡片
            user?.let {
                UserProfileCard(
                    user = it,
                    onEditClick = { /* 编辑资料 */ }
                )
            }

            // 功能列表
            ProfileMenuList(
                onDraftsClick = onNavigateToDrafts,
                onFavoritesClick = { /* 收藏 */ },
                onHistoryClick = { /* 浏览历史 */ },
                onWalletClick = { /* 钱包 */ },
                onLogoutClick = {
                    viewModel.logout(onLogout)
                }
            )
        }
    }
}

@Composable
fun UserProfileCard(
    user: User,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WeiboOrange)
            .padding(16.dp)
    ) {
        // 头像和基本信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.avatarLarge ?: user.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "头像",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 用户名和简介
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.screenName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // 认证标识
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Yellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WeiboOrange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.description ?: "暂无简介",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            // 编辑按钮
            OutlinedButton(
                onClick = onEditClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text("编辑资料")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 统计数据
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(count = user.statusesCount.toString(), label = "微博")
            StatItem(count = user.followersCount.toString(), label = "粉丝")
            StatItem(count = user.friendsCount.toString(), label = "关注")
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileMenuList(
    onDraftsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onWalletClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 我的内容
        MenuSection(title = "我的内容") {
            MenuItem(
                icon = Icons.Outlined.Edit,
                title = "草稿箱",
                onClick = onDraftsClick
            )
            MenuItem(
                icon = Icons.Outlined.FavoriteBorder,
                title = "我的收藏",
                onClick = onFavoritesClick
            )
            MenuItem(
                icon = Icons.Outlined.History,
                title = "浏览历史",
                onClick = onHistoryClick
            )
        }

        Divider()

        // 更多
        MenuSection(title = "更多") {
            MenuItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "我的钱包",
                onClick = onWalletClick
            )
            MenuItem(
                icon = Icons.Outlined.HelpOutline,
                title = "帮助与反馈",
                onClick = { /* 帮助 */ }
            )
        }

        Divider()

        // 退出登录
        MenuItem(
            icon = Icons.Outlined.Logout,
            title = "退出登录",
            tint = Color.Red,
            onClick = onLogoutClick
        )
    }
}

@Composable
fun MenuSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = tint,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
