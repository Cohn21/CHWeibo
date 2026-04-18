package com.chweibo.android.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.Comment
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.components.emoji.EmojiTextField
import com.chweibo.android.ui.theme.TextGray
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.CommentsViewModel
import com.chweibo.android.ui.viewmodel.UiEvent
import com.chweibo.android.utils.TimeUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    weiboId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val comments by viewModel.comments.collectAsState()
    val weibo by viewModel.weibo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(weiboId) {
        viewModel.loadWeiboAndComments(weiboId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("评论") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            CommentInputBar(
                text = commentText,
                onTextChange = viewModel::updateCommentText,
                onSend = { viewModel.postComment(weiboId) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 原微博
            weibo?.let { post ->
                item {
                    OriginalWeiboCard(
                        weibo = post,
                        onUserClick = { post.user?.id?.let { onNavigateToUserProfile(it) } }
                    )
                    Divider()
                }
            }

            // 评论数标题
            item {
                Text(
                    text = "评论 (${weibo?.commentsCount ?: 0})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 评论列表
            if (isLoading && comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onUserClick = { comment.user?.id?.let { onNavigateToUserProfile(it) } },
                        onReplyClick = { viewModel.replyTo(comment) }
                    )
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun OriginalWeiboCard(
    weibo: WeiboPost,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        // 用户信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = weibo.user?.profileImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick() },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = weibo.user?.screenName ?: "未知用户",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = TimeUtils.formatTime(weibo.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 微博内容
        Text(
            text = weibo.text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onUserClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 头像
        AsyncImage(
            model = comment.user?.profileImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { onUserClick() },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // 用户名
            Text(
                text = comment.user?.screenName ?: "未知用户",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 评论内容
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )

            // 回复的评论
            comment.replyComment?.let { reply ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "回复 @${reply.user?.screenName}: ${reply.text}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 时间和操作
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatTime(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )

                Row {
                    TextButton(
                        onClick = onReplyClick,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "回复",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }
        }

        // 点赞
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { /* 点赞评论 */ }) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = "写评论...",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    tint = if (text.isNotBlank()) WeiboOrange else TextGray
                )
            }
        }
    }
}
