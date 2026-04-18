package com.chweibo.android.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.HomeViewModel
import com.chweibo.android.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: (Long) -> Unit,
    onNavigateToImageViewer: (List<String>, Int) -> Unit,
    onNavigateToRepost: (Long) -> Unit = {},
    onNavigateToComments: (Long) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val timeline = viewModel.timeline.collectAsLazyPagingItems()
    val refreshing by viewModel.refreshing.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            timeline.refresh()
            viewModel.refresh()
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.Navigate -> {
                    if (event.route == "login") onLogout()
                }
                else -> {}
            }
        }
    }

    var accountMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "微博",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { accountMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "账号管理",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = accountMenuExpanded,
                        onDismissRequest = { accountMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("退出登录") },
                            onClick = {
                                accountMenuExpanded = false
                                viewModel.logout()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("切换账号 / 登录") },
                            onClick = {
                                accountMenuExpanded = false
                                viewModel.logout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WeiboOrange,
                    titleContentColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigate to post */ },
                containerColor = WeiboOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "发布")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when (timeline.loadState.refresh) {
                is LoadState.Loading -> {
                    if (!refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is LoadState.Error -> {
                    val error = timeline.loadState.refresh as LoadState.Error
                    ErrorView(
                        message = error.error.localizedMessage ?: "加载失败",
                        onRetry = { timeline.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    WeiboList(
                        weibos = timeline,
                        onWeiboClick = onNavigateToDetail,
                        onUserClick = onNavigateToProfile,
                        onImageClick = onNavigateToImageViewer,
                        onLikeClick = { viewModel.likeWeibo(it) },
                        onRepostClick = onNavigateToRepost,
                        onCommentClick = onNavigateToComments
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = WeiboOrange
            )
        }
    }
}

@Composable
fun WeiboList(
    weibos: LazyPagingItems<WeiboPost>,
    onWeiboClick: (String) -> Unit,
    onUserClick: (Long) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onLikeClick: (Long) -> Unit,
    onRepostClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            count = weibos.itemCount,
            key = weibos.itemKey { it.id }
        ) { index ->
            val weibo = weibos[index]
            if (weibo != null) {
                WeiboCard(
                    weibo = weibo,
                    onWeiboClick = { onWeiboClick(weibo.idStr ?: weibo.id.toString()) },
                    onUserClick = { weibo.user?.id?.let { onUserClick(it) } },
                    onImageClick = { urls, idx -> onImageClick(urls, idx) },
                    onLikeClick = { onLikeClick(weibo.id) },
                    onRepostClick = { onRepostClick(weibo.id) },
                    onCommentClick = { onCommentClick(weibo.id) }
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }

        // 加载更多
        when (weibos.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorView(
                        message = "加载更多失败",
                        onRetry = { weibos.retry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
