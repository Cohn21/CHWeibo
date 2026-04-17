package com.chweibo.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.components.ImageGrid
import com.chweibo.android.ui.theme.RepostBackground
import com.chweibo.android.ui.theme.TextGray
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.utils.TimeUtils

@Composable
fun WeiboCard(
    weibo: WeiboPost,
    onWeiboClick: () -> Unit,
    onUserClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onLikeClick: () -> Unit,
    onRepostClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onWeiboClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // 用户信息行
        UserInfoRow(
            weibo = weibo,
            onUserClick = onUserClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 微博内容
        WeiboContent(
            weibo = weibo,
            onImageClick = onImageClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 来源和时间
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TimeUtils.formatTime(weibo.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
            if (weibo.getSourceText().isNotEmpty()) {
                Text(
                    text = " 来自 ${weibo.getSourceText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 操作栏
        ActionBar(
            repostsCount = weibo.repostsCount,
            commentsCount = weibo.commentsCount,
            attitudesCount = weibo.attitudesCount,
            favorited = weibo.favorited,
            onRepostClick = onRepostClick,
            onCommentClick = onCommentClick,
            onLikeClick = onLikeClick
        )
    }
}

@Composable
fun UserInfoRow(
    weibo: WeiboPost,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = weibo.user

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user?.profileImageUrl ?: user?.avatarLarge)
                .crossfade(true)
                .build(),
            contentDescription = "用户头像",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onUserClick() },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 用户名和认证信息
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.screenName ?: "未知用户",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 认证图标
                if (user?.verified == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(verifiedType = user.verifiedType)
                }
            }

            // 简介或位置
            if (!user?.description.isNullOrEmpty()) {
                Text(
                    text = user?.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 关注按钮
        if (user != null && !user.following) {
            OutlinedButton(
                onClick = { /* 关注用户 */ },
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "+ 关注",
                    style = MaterialTheme.typography.bodySmall,
                    color = WeiboOrange
                )
            }
        }
    }
}

@Composable
fun VerifiedBadge(verifiedType: Int, modifier: Modifier = Modifier) {
    // verifiedType: 0-个人认证, 1,2,3,4,5,6,7-机构认证
    val color = when (verifiedType) {
        0 -> Color(0xFFFFA500) // 橙色 - 个人认证
        else -> Color(0xFF4A90E2) // 蓝色 - 机构认证
    }

    Box(
        modifier = modifier
            .size(14.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeiboContent(
    weibo: WeiboPost,
    onImageClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 微博文本
        val displayText = if (weibo.isLongText && weibo.text.length > 140) {
            weibo.text.take(140) + "...全文"
        } else {
            weibo.text
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )

        // 图片网格
        val pics = weibo.getThumbnailPics()
        if (pics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ImageGrid(
                images = pics,
                onImageClick = { index ->
                    onImageClick(weibo.getAllPics(), index)
                }
            )
        }

        // 转发内容
        weibo.retweetedStatus?.let { retweeted ->
            Spacer(modifier = Modifier.height(8.dp))
            RetweetedContent(
                weibo = retweeted,
                onImageClick = onImageClick
            )
        }
    }
}

@Composable
fun RetweetedContent(
    weibo: WeiboPost,
    onImageClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RepostBackground, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        // 转发用户信息
        val userName = weibo.user?.screenName ?: "未知用户"
        val text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = WeiboOrange)) {
                append("@$userName")
            }
            append(": ${weibo.text}")
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )

        // 转发图片
        val pics = weibo.getThumbnailPics()
        if (pics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            ImageGrid(
                images = pics,
                onImageClick = { index ->
                    onImageClick(weibo.getAllPics(), index)
                }
            )
        }
    }
}

@Composable
fun ActionBar(
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 转发
        ActionButton(
            icon = Icons.Outlined.Repeat,
            count = repostsCount,
            onClick = onRepostClick
        )

        // 评论
        ActionButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            count = commentsCount,
            onClick = onCommentClick
        )

        // 点赞
        ActionButton(
            icon = if (favorited) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
            count = attitudesCount,
            onClick = onLikeClick,
            tint = if (favorited) WeiboOrange else TextGray
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    onClick: () -> Unit,
    tint: Color = TextGray,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.bodySmall,
            color = tint
        )
    }
}

fun formatCount(count: Int): String {
    return when {
        count == 0 -> ""
        count >= 10000 -> "${count / 10000}万"
        else -> count.toString()
    }
}
