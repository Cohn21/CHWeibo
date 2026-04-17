package com.chweibo.android.ui.screens.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.chweibo.android.ui.theme.WeiboOrange
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPreviewScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用图标预览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "微博客户端应用图标",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 120x120 大图标
            IconPreviewBox(
                size = 120,
                title = "大图标 (120×120)",
                subtitle = "用于应用展示"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 80x80 中图标
            IconPreviewBox(
                size = 80,
                title = "中图标 (80×80)",
                subtitle = "用于详情页面"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 16x16 小图标
            IconPreviewBox(
                size = 16,
                title = "小图标 (16×16)",
                subtitle = "用于列表展示"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    saveIconsToFile(context)
                    showSavedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = WeiboOrange)
            ) {
                Text("保存图标到设备")
            }

            if (showSavedMessage) {
                Text(
                    text = "图标已保存到 Downloads/WeiboIcons/ 目录",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "提示：打开 D:\\work\\One\\icon_generator.html 文件，\n可以下载用于微博开放平台上传的图标文件。",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun IconPreviewBox(
    size: Int,
    title: String,
    subtitle: String
) {
    val bitmap = remember(size) { generateWeiboIcon(size) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(if (size > 20) 16.dp else 4.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

fun generateWeiboIcon(size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val weiboOrange = WeiboOrange.toArgb()

    // 绘制圆形背景
    paint.color = weiboOrange
    val radius = (size / 2f) - 1
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    // 绘制 "微" 字简化版
    paint.color = android.graphics.Color.WHITE

    val padding = (size * 0.25).toInt()
    val lineWidth = (size * 0.12).toInt().coerceAtLeast(1)

    if (size <= 16) {
        // 16x16 简化版本
        val strokeWidth = 2f.coerceAtMost(size * 0.15f)
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        val centerX = size / 2f
        val centerY = size / 2f
        canvas.drawLine(centerX - 3, centerY - 3, centerX, centerY + 3, paint)
        canvas.drawLine(centerX, centerY + 3, centerX + 3, centerY - 3, paint)
    } else {
        // 绘制横线和竖线
        val topY = padding.toFloat()
        val bottomY = size - padding
        val midY = size / 2f
        val leftX = padding.toFloat()
        val rightX = size - padding
        val centerX = size / 2f

        paint.style = Paint.Style.FILL

        // 上横
        canvas.drawRect(leftX, topY, rightX.toFloat(), (topY + lineWidth).toFloat(), paint)
        // 中横
        canvas.drawRect(leftX, midY - lineWidth / 2f, rightX.toFloat(), midY + lineWidth / 2f, paint)
        // 下横
        canvas.drawRect(leftX, (bottomY - lineWidth).toFloat(), rightX.toFloat(), bottomY.toFloat(), paint)
        // 左边竖
        canvas.drawRect(leftX + 2, topY, leftX + 2 + lineWidth.toFloat(), bottomY.toFloat(), paint)
        // 右边竖
        canvas.drawRect(rightX - lineWidth.toFloat() - 2, topY, rightX.toFloat() - 2, bottomY.toFloat(), paint)
        // 中间竖
        canvas.drawRect(centerX - lineWidth / 2f, topY, centerX + lineWidth / 2f, bottomY.toFloat(), paint)
    }

    return bitmap
}

fun saveIconsToFile(context: Context) {
    val dir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "WeiboIcons")
    dir.mkdirs()

    val sizes = listOf(16, 80, 120)
    sizes.forEach { size ->
        val bitmap = generateWeiboIcon(size)
        val file = File(dir, "weibo_icon_${size}x${size}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
