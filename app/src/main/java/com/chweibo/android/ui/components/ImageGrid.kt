package com.chweibo.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ImageGrid(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (images.size) {
        0 -> {}
        1 -> SingleImage(
            image = images[0],
            onClick = { onImageClick(0) },
            modifier = modifier
        )
        2, 3 -> Grid2or3Images(
            images = images,
            onImageClick = onImageClick,
            modifier = modifier
        )
        4 -> Grid4Images(
            images = images,
            onImageClick = onImageClick,
            modifier = modifier
        )
        else -> GridMoreImages(
            images = images.take(9),
            onImageClick = onImageClick,
            modifier = modifier
        )
    }
}

@Composable
fun SingleImage(
    image: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth(0.6f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun Grid2or3Images(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = 4.dp
    val imageSize = when (images.size) {
        2 -> 160.dp
        else -> 100.dp
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        images.forEachIndexed { index, image ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onImageClick(index) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun Grid4Images(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = 4.dp
    val imageSize = 120.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            images.take(2).forEachIndexed { index, image ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onImageClick(index) },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            images.drop(2).forEachIndexed { index, image ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onImageClick(index + 2) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun GridMoreImages(
    images: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = 4.dp
    val imageSize = 100.dp

    val rows = images.chunked(3)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEachIndexed { rowIndex, rowImages ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowImages.forEachIndexed { colIndex, image ->
                    val index = rowIndex * 3 + colIndex
                    BoxWithIndicator(
                        image = image,
                        index = index,
                        totalCount = images.size,
                        onClick = { onImageClick(index) },
                        modifier = Modifier.size(imageSize)
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithIndicator(
    image: String,
    index: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )

        // 显示更多图片提示
        if (index == 8 && totalCount > 9) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onClick() },
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "+${totalCount - 9}",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
