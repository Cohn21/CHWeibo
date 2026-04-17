package com.chweibo.android.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chweibo.android.data.model.Comment
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.screens.comments.CommentItem
import com.chweibo.android.ui.screens.home.WeiboCard
import com.chweibo.android.ui.theme.TextGray
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.WeiboDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeiboDetailScreen(
    weiboId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit,
    onNavigateToImageViewer: (List<String>, Int) -> Unit,
    onNavigateToComments: (Long) -> Unit,
    viewModel: WeiboDetailViewModel = hiltViewModel()
) {
    val weibo by viewModel.weibo.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val commentText by viewModel.commentText.collectAsState()

    LaunchedEffect(weiboId) {
        viewModel.loadWeiboDetail(weiboId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("微博详情") },
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
            DetailBottomBar(
                weibo = weibo,
                commentText = commentText,
                onCommentTextChange = viewModel::updateCommentText,
                onSendComment = { viewModel.postComment() },
                onLikeClick = { viewModel.toggleLike() },
                onRepostClick = { /* 转发 */ },
                onCommentClick = { onNavigateToComments(weiboId) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 微博内容
            weibo?.let { post ->
                item {
                    WeiboCard(
                        weibo = post,
                        onWeiboClick = { },
                        onUserClick = { post.user?.id?.let { onNavigateToUserProfile(it) } },
                        onImageClick = onNavigateToImageViewer,
                        onLikeClick = { viewModel.toggleLike() },
                        onRepostClick = { /* 转发 */ },
                        onCommentClick = { onNavigateToComments(weiboId) }
                    )
                }
            }

            // 评论标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "评论 ${weibo?.commentsCount ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "转发 ${weibo?.repostsCount ?: 0}  赞 ${weibo?.attitudesCount ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                Divider()
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
            } else if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无评论，快来抢沙发吧~", color = TextGray)
                    }
                }
            } else {
                items(comments.take(10)) { comment ->
                    CommentItem(
                        comment = comment,
                        onUserClick = { comment.user?.id?.let { onNavigateToUserProfile(it) } },
                        onReplyClick = { viewModel.replyTo(comment) }
                    )
                    Divider(thickness = 0.5.dp)
                }

                // 查看更多评论
                if (comments.size > 10) {
                    item {
                        TextButton(
                            onClick = { onNavigateToComments(weiboId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("查看全部评论 >")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailBottomBar(
    weibo: WeiboPost?,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onLikeClick: () -> Unit,
    onRepostClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 评论输入框
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                placeholder = { Text("写评论...") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            // 操作按钮
            Row {
                IconButton(onClick = onRepostClick) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Repeat, contentDescription = "转发")
                        Text(
                            text = "${weibo?.repostsCount ?: 0}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                IconButton(onClick = onCommentClick) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "评论")
                        Text(
                            text = "${weibo?.commentsCount ?: 0}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                IconButton(onClick = onLikeClick) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (weibo?.favorited == true)
                                Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "赞",
                            tint = if (weibo?.favorited == true) WeiboOrange else TextGray
                        )
                        Text(
                            text = "${weibo?.attitudesCount ?: 0}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = { content() }
    )
}
