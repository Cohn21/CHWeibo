package com.chweibo.android.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.User
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.screens.home.WeiboCard
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserProfileScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToWeiboDetail: (String) -> Unit,
    onNavigateToImageViewer: (List<String>, Int) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val weibos by viewModel.weibos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState { 3 }
    val tabTitles = listOf("微博", "相册", "资料")

    LaunchedEffect(userId) {
        viewModel.loadUserInfo(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.screenName ?: "用户资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 更多选项 */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 用户信息头部
            user?.let { userInfo ->
                item {
                    UserProfileHeader(
                        user = userInfo,
                        isFollowing = isFollowing,
                        onFollowClick = { viewModel.toggleFollow(userId) }
                    )
                }
            }

            // Tab 切换
            item {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // 内容区域
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.height(600.dp)
                ) { page ->
                    when (page) {
                        0 -> UserWeiboList(
                            weibos = weibos,
                            isLoading = isLoading,
                            onWeiboClick = onNavigateToWeiboDetail,
                            onImageClick = onNavigateToImageViewer
                        )
                        1 -> UserPhotoGrid(userId = userId)
                        2 -> UserInfoDetail(user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    user: User,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // 头像和基本信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarLarge ?: user.profileImageUrl,
                contentDescription = "头像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.screenName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(verifiedType = user.verifiedType)
                    }
                }

                if (!user.verifiedReason.isNullOrEmpty()) {
                    Text(
                        text = user.verifiedReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = WeiboOrange
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@${user.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 简介
        if (!user.description.isNullOrEmpty()) {
            Text(
                text = user.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // 位置
        if (!user.location.isNullOrEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 统计数据
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(count = user.statusesCount.toString(), label = "微博")
            StatColumn(count = user.friendsCount.toString(), label = "关注")
            StatColumn(count = user.followersCount.toString(), label = "粉丝")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 关注按钮
        Button(
            onClick = onFollowClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing) Color.Gray else WeiboOrange
            )
        ) {
            Text(if (isFollowing) "已关注" else "+ 关注")
        }
    }
}

@Composable
fun VerifiedBadge(verifiedType: Int) {
    val color = when (verifiedType) {
        0 -> Color(0xFFFFA500)
        else -> Color(0xFF4A90E2)
    }
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun UserWeiboList(
    weibos: List<WeiboPost>,
    isLoading: Boolean,
    onWeiboClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    if (isLoading && weibos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (weibos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无微博", color = Color.Gray)
        }
    } else {
        LazyColumn {
            items(weibos) { weibo ->
                WeiboCard(
                    weibo = weibo,
                    onWeiboClick = { onWeiboClick(weibo.idStr ?: weibo.id.toString()) },
                    onUserClick = { },
                    onImageClick = onImageClick,
                    onLikeClick = { },
                    onRepostClick = { },
                    onCommentClick = { }
                )
                Divider()
            }
        }
    }
}

@Composable
fun UserPhotoGrid(userId: Long) {
    // 实现相册网格
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("相册功能开发中", color = Color.Gray)
    }
}

@Composable
fun UserInfoDetail(user: User?) {
    if (user == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        InfoItem(title = "昵称", value = user.screenName)
        InfoItem(title = "性别", value = when (user.gender) {
            "m" -> "男"
            "f" -> "女"
            else -> "保密"
        })
        InfoItem(title = "所在地", value = user.location ?: "未设置")
        InfoItem(title = "简介", value = user.description ?: "暂无简介")
        InfoItem(title = "注册时间", value = user.createdAt ?: "未知")
    }
}

@Composable
fun InfoItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider()
}