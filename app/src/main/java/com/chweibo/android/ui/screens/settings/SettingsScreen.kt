package com.chweibo.android.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chweibo.android.ui.theme.WeiboOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var darkMode by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var autoPlayVideo by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            // 通用设置
            SettingsSection(title = "通用设置") {
                SettingsSwitchItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "深色模式",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
                SettingsSwitchItem(
                    icon = Icons.Outlined.Notifications,
                    title = "消息通知",
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
                SettingsSwitchItem(
                    icon = Icons.Outlined.PlayCircle,
                    title = "自动播放视频",
                    checked = autoPlayVideo,
                    onCheckedChange = { autoPlayVideo = it }
                )
            }

            Divider()

            // 账号与安全
            SettingsSection(title = "账号与安全") {
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = "修改密码",
                    onClick = {
                        Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "隐私设置",
                    onClick = {
                        Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Divider()

            // 其他
            SettingsSection(title = "其他") {
                SettingsItem(
                    icon = Icons.Outlined.Storage,
                    title = "清除缓存",
                    subtitle = "12.5 MB",
                    onClick = {
                        Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "关于",
                    subtitle = "版本 1.0.0",
                    onClick = {
                        Toast.makeText(context, "微博客户端 v1.0.0", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部版权信息
            Text(
                text = "微博客户端 1.0.0\n基于微博开放平台",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WeiboOrange,
                checkedTrackColor = WeiboOrange.copy(alpha = 0.5f)
            )
        )
    }
}
