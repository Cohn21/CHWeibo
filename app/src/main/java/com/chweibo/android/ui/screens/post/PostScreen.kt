package com.chweibo.android.ui.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onPostSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    val content by viewModel.content.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()
    val canPost by viewModel.canPost.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发微博") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.postWeibo() },
                        enabled = canPost && !isPosting
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "发送",
                                color = if (canPost) WeiboOrange else Color.Gray
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
            // 文本输入区
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
                                text = "分享新鲜事...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 已选图片展示
            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        SelectedImageItem(
                            uri = uri,
                            onRemove = { viewModel.removeImage(uri) }
                        )
                    }
                }
            }

            Divider()

            // 底部工具栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = selectedImages.size < 9
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "添加图片",
                        tint = WeiboOrange
                    )
                }
                IconButton(onClick = { /* @功能 */ }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "@",
                        tint = WeiboOrange
                    )
                }
                IconButton(onClick = { /* 话题 */ }) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "话题",
                        tint = WeiboOrange
                    )
                }
                IconButton(onClick = { /* 表情 */ }) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "表情",
                        tint = WeiboOrange
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedImageItem(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
