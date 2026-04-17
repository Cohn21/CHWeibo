package com.chweibo.android.ui.screens.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.Layout
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chweibo.android.ui.theme.WeiboOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.fillMaxWidth(0.95f)
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
            // 热搜榜单
            item {
                HotSearchSection()
            }

            // 功能入口
            item {
                FeatureGrid()
            }

            // 推荐话题
            item {
                RecommendedTopicsSection()
            }
        }
    }
}

@Composable
fun SearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索微博、找人",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun HotSearchSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "微博热搜",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* 查看全部 */ }) {
                Text("查看全部", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 热搜列表（示例数据）
        val hotSearches = listOf(
            HotSearchItem("1", "#今日热点#", true, "新"),
            HotSearchItem("2", "#科技新闻#", false, ""),
            HotSearchItem("3", "#娱乐八卦#", true, "热"),
            HotSearchItem("4", "#体育赛事#", false, ""),
            HotSearchItem("5", "#美食推荐#", false, ""),
        )

        hotSearches.forEach { item ->
            HotSearchRow(item = item)
        }
    }
}

data class HotSearchItem(
    val rank: String,
    val title: String,
    val isHot: Boolean,
    val tag: String
)

@Composable
fun HotSearchRow(item: HotSearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 打开话题 */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.rank,
            modifier = Modifier.width(32.dp),
            color = when (item.rank) {
                "1" -> Color(0xFFFF6B6B)
                "2" -> Color(0xFFFF9F43)
                "3" -> Color(0xFFFFD93D)
                else -> Color.Gray
            },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        if (item.isHot) {
            Surface(
                color = WeiboOrange.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.tag,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = WeiboOrange,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun FeatureGrid() {
    val features = listOf(
        FeatureItem("超话社区", Icons.Default.Forum),
        FeatureItem("热门微博", Icons.Default.Whatshot),
        FeatureItem("同城", Icons.Default.LocationOn),
        FeatureItem("榜单", Icons.Default.EmojiEvents),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        features.forEach { feature ->
            FeatureItemView(feature = feature)
        }
    }
}

data class FeatureItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun FeatureItemView(feature: FeatureItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* 导航到对应页面 */ }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = WeiboOrange.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = WeiboOrange,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feature.title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun RecommendedTopicsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "推荐话题",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 话题标签流
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val topics = listOf(
                "#摄影#", "#旅行#", "#美食#", "#健身#",
                "#读书#", "#音乐#", "#电影#", "#动漫#"
            )
            topics.forEach { topic ->
                TopicChip(topic = topic)
            }
        }
    }
}

@Composable
fun TopicChip(topic: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.clickable { /* 打开话题 */ }
    ) {
        Text(
            text = topic,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = 8
        val vGapPx = 8
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val width = rowWidths.maxOrNull()?.coerceIn(constraints.minWidth, constraints.maxWidth) ?: constraints.minWidth
        val height = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx

        layout(width, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = when (horizontalArrangement) {
                    Arrangement.End -> width - rowWidths[rowIndex]
                    Arrangement.Center -> (width - rowWidths[rowIndex]) / 2
                    else -> 0
                }

                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }

                y += rowHeights[rowIndex] + vGapPx
            }
        }
    }
}
