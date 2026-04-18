package com.chweibo.android.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.Comment
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.components.ImageGrid
import com.chweibo.android.ui.components.video.VideoPlayer
import com.chweibo.android.ui.components.video.VideoThumbnail
import com.chweibo.android.ui.screens.comments.CommentItem
import com.chweibo.android.ui.screens.home.VerifiedBadge
import com.chweibo.android.ui.screens.home.formatCount
import com.chweibo.android.ui.theme.RepostBackground
import com.chweibo.android.ui.theme.RepostBackgroundDark
import com.chweibo.android.ui.theme.TextGray
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.UiEvent
import com.chweibo.android.ui.viewmodel.WeiboDetailViewModel
import com.chweibo.android.utils.TimeUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeiboDetailScreen(
    weiboId: String,
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    LaunchedEffect(weiboId) {
        viewModel.loadWeiboDetail(weiboId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = weibo?.user?.screenName ?: "Weibo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (weibo != null) {
                CommentInputBar(
                    weibo = weibo,
                    commentText = commentText,
                    onCommentTextChange = viewModel::updateCommentText,
                    onSendComment = viewModel::postComment,
                    onLikeClick = viewModel::toggleLike
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading && weibo == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                weibo == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = TextGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Load failed",
                                    color = TextGray,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Weibo ID: $weiboId",
                                    color = TextGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadWeiboDetail(weiboId) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                else -> {
                    val post = weibo!!

                    item {
                        WeiboDetailHeader(
                            weibo = post,
                            onUserClick = { post.user?.id?.let(onNavigateToUserProfile) },
                            onImageClick = onNavigateToImageViewer
                        )
                    }

                    item {
                        ActionBarSection(
                            repostsCount = post.repostsCount,
                            commentsCount = post.commentsCount,
                            attitudesCount = post.attitudesCount,
                            favorited = post.favorited,
                            onRepostClick = { },
                            onCommentClick = { onNavigateToComments(post.id) },
                            onLikeClick = viewModel::toggleLike
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${post.commentsCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    when {
                        isLoading && comments.isEmpty() -> {
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
                        }

                        comments.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No comments yet",
                                        color = TextGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        else -> {
                            items(comments, key = { it.id }) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onUserClick = { comment.user?.id?.let(onNavigateToUserProfile) },
                                    onReplyClick = { viewModel.replyTo(comment) }
                                )
                                Divider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 72.dp)
                                )
                            }

                            if (comments.size >= 10) {
                                item {
                                    TextButton(
                                        onClick = { onNavigateToComments(post.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Text("View all comments >")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeiboDetailHeader(
    weibo: WeiboPost,
    onUserClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(weibo.user?.profileImageUrl ?: weibo.user?.avatarLarge)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick() },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = weibo.user?.screenName ?: "Unknown user",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (weibo.user?.verified == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(verifiedType = weibo.user.verifiedType)
                    }
                }
                Text(
                    text = TimeUtils.formatTime(weibo.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            if (weibo.user != null && !weibo.user.following) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.height(32.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("+ Follow", style = MaterialTheme.typography.bodySmall, color = WeiboOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = weibo.text,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (weibo.hasVideo()) {
            Spacer(modifier = Modifier.height(12.dp))
            VideoPlayer(
                videoUrl = weibo.videoUrl ?: "",
                autoPlay = false
            )
        } else {
            val pics = weibo.getThumbnailPics()
            if (pics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ImageGrid(
                    images = pics,
                    onImageClick = { index -> onImageClick(weibo.getAllPics(), index) }
                )
            }
        }

        weibo.retweetedStatus?.let { retweeted ->
            Spacer(modifier = Modifier.height(12.dp))
            RetweetedSection(
                weibo = retweeted,
                onImageClick = onImageClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (weibo.getSourceText().isNotEmpty()) {
            Text(
                text = "Source ${weibo.getSourceText()}",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }
    }
}

@Composable
fun RetweetedSection(
    weibo: WeiboPost,
    onImageClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSystemInDarkTheme()) RepostBackgroundDark else RepostBackground

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        val text = buildAnnotatedString {
            withStyle(SpanStyle(color = WeiboOrange, fontWeight = FontWeight.Medium)) {
                append("@${weibo.user?.screenName ?: "Unknown user"}")
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(": ${weibo.text}")
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )

        if (weibo.hasVideo()) {
            Spacer(modifier = Modifier.height(8.dp))
            VideoThumbnail(
                videoUrl = weibo.videoUrl ?: "",
                onClick = { /* TODO: play video */ }
            )
        } else {
            val pics = weibo.getThumbnailPics()
            if (pics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ImageGrid(
                    images = pics,
                    onImageClick = { index -> onImageClick(weibo.getAllPics(), index) }
                )
            }
        }
    }
}

@Composable
fun ActionBarSection(
    repostsCount: Int,
    commentsCount: Int,
    attitudesCount: Int,
    favorited: Boolean,
    onRepostClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionItem(Icons.Outlined.Repeat, repostsCount, "Repost", onRepostClick, TextGray)
        ActionItem(Icons.Outlined.ChatBubbleOutline, commentsCount, "Comment", onCommentClick, TextGray)
        ActionItem(
            if (favorited) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
            attitudesCount,
            "Like",
            onLikeClick,
            if (favorited) WeiboOrange else TextGray
        )
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}

@Composable
fun CommentInputBar(
    weibo: WeiboPost?,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                placeholder = { Text("Write a comment...") },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = if (commentText.isNotBlank()) {
                    {
                        IconButton(onClick = onSendComment) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = WeiboOrange
                            )
                        }
                    }
                } else {
                    null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    imageVector = if (weibo?.favorited == true) {
                        Icons.Outlined.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Like",
                    tint = if (weibo?.favorited == true) WeiboOrange else TextGray,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = formatCount(weibo?.attitudesCount ?: 0),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (weibo?.favorited == true) WeiboOrange else TextGray
                )
            }
        }
    }
}
