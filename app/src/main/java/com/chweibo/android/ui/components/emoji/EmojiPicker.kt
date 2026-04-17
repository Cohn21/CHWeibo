package com.chweibo.android.ui.components.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chweibo.android.data.model.Emotion

@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(Emotion.CATEGORY_DEFAULT) }

    val categories = listOf(
        Emotion.CATEGORY_DEFAULT to "默认",
        Emotion.CATEGORY_EMOJI to "Emoji",
        Emotion.CATEGORY_LXZ to "冷兔",
        Emotion.CATEGORY_DZZ to "暴走",
        Emotion.CATEGORY_XHJ to "小黄鸡"
    )

    // 示例表情数据
    val emotions = remember { generateSampleEmotions() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 分类标签
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (category, title) ->
                CategoryChip(
                    title = title,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Divider()

        // 表情网格
        val filteredEmotions = emotions.filter { it.category == selectedCategory }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredEmotions) { emotion ->
                EmojiItem(
                    emotion = emotion,
                    onClick = { onEmojiSelected(emotion.phrase) }
                )
            }
        }

        Divider()

        // 底部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { /* 切换到常用 */ }) {
                Text("常用表情")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmojiItem(
    emotion: Emotion,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (emotion.type == "face") {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(emotion.url)
                    .crossfade(true)
                    .build(),
                contentDescription = emotion.phrase,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = emotion.phrase,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// 生成示例表情数据
private fun generateSampleEmotions(): List<Emotion> {
    return listOf(
        Emotion("[微笑]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[撇嘴]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[色]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[发呆]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[得意]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[流泪]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[害羞]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[闭嘴]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[睡]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[大哭]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[尴尬]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[发怒]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[调皮]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[呲牙]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[惊讶]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[难过]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[酷]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[冷汗]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[抓狂]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[吐]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[偷笑]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[可爱]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[白眼]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[傲慢]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[饥饿]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[困]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[惊恐]", "face", "", category = Emotion.CATEGORY_DEFAULT),
        Emotion("[流汗]", "face", "", category = Emotion.CATEGORY_DEFAULT),

        // Emoji
        Emotion("😀", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😃", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😄", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😁", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😆", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😅", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🤣", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😂", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🙂", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🙃", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😉", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😊", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😇", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🥰", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😍", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🤩", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😘", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😗", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("☺️", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😚", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😙", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🥲", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😋", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😛", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😜", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🤪", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("😝", "emoji", "", category = Emotion.CATEGORY_EMOJI),
        Emotion("🤑", "emoji", "", category = Emotion.CATEGORY_EMOJI)
    )
}

@Composable
fun EmojiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请输入内容..."
) {
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                    Text("😊")
                }
            }
        )

        if (showEmojiPicker) {
            EmojiPicker(
                onEmojiSelected = { emoji ->
                    onValueChange(value + emoji)
                },
                onDelete = {
                    if (value.isNotEmpty()) {
                        onValueChange(value.dropLast(1))
                    }
                }
            )
        }
    }
}