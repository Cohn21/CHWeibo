package com.chweibo.android.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.Layout
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.screens.home.WeiboCard
import com.chweibo.android.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit,
    onNavigateToWeiboDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hotSearches by viewModel.hotSearches.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索微博、找人") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.search(searchQuery)
                                }
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
        ) {
            if (searchQuery.isBlank() && searchResults.isEmpty()) {
                // 显示搜索历史和热搜
                SearchSuggestions(
                    hotSearches = hotSearches,
                    searchHistory = searchHistory,
                    onHotSearchClick = { query ->
                        searchQuery = query
                        viewModel.search(query)
                    },
                    onHistoryClick = { query ->
                        searchQuery = query
                        viewModel.search(query)
                    },
                    onClearHistory = { viewModel.clearSearchHistory() }
                )
            } else if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 显示搜索结果
                LazyColumn {
                    items(searchResults) { weibo ->
                        WeiboCard(
                            weibo = weibo,
                            onWeiboClick = { onNavigateToWeiboDetail(weibo.id) },
                            onUserClick = { weibo.user?.id?.let { onNavigateToUserProfile(it) } },
                            onImageClick = { _, _ -> },
                            onLikeClick = { },
                            onRepostClick = { },
                            onCommentClick = { }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSuggestions(
    hotSearches: List<String>,
    searchHistory: List<String>,
    onHotSearchClick: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 搜索历史
        if (searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "搜索历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearHistory) {
                    Text("清除")
                }
            }

            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                searchHistory.forEach { query ->
                    SearchHistoryChip(
                        query = query,
                        onClick = { onHistoryClick(query) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // 热搜榜
        Text(
            text = "微博热搜",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        hotSearches.forEachIndexed { index, topic ->
            HotSearchItem(
                rank = index + 1,
                topic = topic,
                onClick = { onHotSearchClick(topic) }
            )
        }
    }
}

@Composable
fun SearchHistoryChip(
    query: String,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = query,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HotSearchItem(
    rank: Int,
    topic: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            modifier = Modifier.width(32.dp),
            color = when (rank) {
                1 -> Color(0xFFFF6B6B)
                2 -> Color(0xFFFF9F43)
                3 -> Color(0xFFFFD93D)
                else -> Color.Gray
            },
            fontWeight = FontWeight.Bold
        )
        Text(
            text = topic,
            style = MaterialTheme.typography.bodyMedium
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

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
        }

        val width = constraints.maxWidth
        val height = rows.size * (rows.firstOrNull()?.firstOrNull()?.height ?: 0) +
                (rows.size - 1).coerceAtLeast(0) * vGapPx

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

                y += row.firstOrNull()?.height?.plus(vGapPx) ?: 0
            }
        }
    }
}
