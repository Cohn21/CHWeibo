package com.chweibo.android.ui.screens.login

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.chweibo.android.BuildConfig
import com.chweibo.android.ui.theme.WeiboOrange
import com.chweibo.android.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWebView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginViewModel.LoginEvent.NavigateToMain -> onLoginSuccess()
            }
        }
    }

    if (showWebView) {
        // 授权 WebView
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("微博授权") },
                    navigationIcon = {
                        IconButton(onClick = { showWebView = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WeiboOrange,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false

                                    // 检查是否是回调 URL
                                    if (url.startsWith(BuildConfig.WEIBO_REDIRECT_URI)) {
                                        viewModel.handleAuthCallback(url)
                                        return true
                                    }
                                    return false
                                }
                            }
                            loadUrl(viewModel.authUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(androidx.compose.ui.Alignment.Center)
                    )
                }
            }
        }
    } else {
        // 登录引导页面
        LoginGuideScreen(
            onLoginClick = { showWebView = true },
            error = uiState.error
        )
    }
}

@Composable
fun LoginGuideScreen(
    onLoginClick: () -> Unit,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = WeiboOrange,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "微",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "微博客户端",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = WeiboOrange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "登录微博账号，开始你的社交之旅",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WeiboOrange
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "微博账号登录",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "登录即表示你同意《用户协议》和《隐私政策》",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
