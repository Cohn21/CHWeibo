package com.chweibo.android.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.components.emoji.EmojiPicker
import com.chweibo.android.ui.theme.RepostBackground
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.RepostViewModel
import com.chweibo.android.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostScreen(
    weiboId: Long,
    onRepostSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RepostViewModel = hiltViewModel()
) {
    val content by viewModel.content.collectAsState()
    val originalWeibo by viewModel.originalWeibo.collectAsState()
    val isReposting by viewModel.isReposting.collectAsState()
    val canRepost by viewModel.canRepost.collectAsState()
    val isComment by viewModel.isComment.collectAsState()
    var showEmojiPicker by remember { mutableStateOf(false) }

    LaunchedEffect(weiboId) {
        viewModel.loadOriginalWeibo(weiboId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    if (event.route == "back") onRepostSuccess()
                }
                is UiEvent.ShowSnackbar -> {
                    // Snackbar could be shown here if a host state is available
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("转发微博") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.repost(weiboId)
                        },
                        enabled = canRepost && !isReposting
                    ) {
                        if (isReposting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "发送",
                                color = if (canRepost) WeiboOrange else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 输入框
            BasicTextField(
                value = content,
                onValueChange = viewModel::updateContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                text = "说说分享的理由...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 原微博预览
            originalWeibo?.let { weibo ->
                OriginalWeiboPreview(weibo = weibo)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 同时评论选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isComment,
                    onCheckedChange = { viewModel.setIsComment(it) }
                )
                Text(
                    text = "同时评论给 ${originalWeibo?.user?.screenName ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showEmojiPicker) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        viewModel.updateContent(content + emoji)
                    },
                    onDelete = {
                        if (content.isNotEmpty()) {
                            viewModel.updateContent(content.dropLast(1))
                        }
                    }
                )
            }

            // 底部工具栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { /* @功能 */ }) {
                    Icon(Icons.Default.AlternateEmail, contentDescription = "@", tint = WeiboOrange)
                }
                IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = "表情",
                        tint = if (showEmojiPicker) MaterialTheme.colorScheme.primary else WeiboOrange
                    )
                }
            }
        }
    }
}

@Composable
fun OriginalWeiboPreview(
    weibo: WeiboPost,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RepostBackground, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "@${weibo.user?.screenName ?: "未知用户"}",
            fontWeight = FontWeight.Medium,
            color = WeiboOrange,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = weibo.text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3
        )
    }
}