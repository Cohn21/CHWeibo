package com.chweibo.android.ui.components.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.chweibo.android.ui.theme.LinkColor
import com.chweibo.android.ui.theme.WeiboOrange

@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onTopicClick: (String) -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    onUrlClick: (String) -> Unit = {},
    maxLines: Int = Int.MAX_VALUE
) {
    val annotatedString = parseRichText(text)

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        onClick = { offset ->
            annotatedString.getStringAnnotations("TOPIC", offset, offset)
                .firstOrNull()?.let { onTopicClick(it.item) }

            annotatedString.getStringAnnotations("MENTION", offset, offset)
                .firstOrNull()?.let { onMentionClick(it.item) }

            annotatedString.getStringAnnotations("URL", offset, offset)
                .firstOrNull()?.let { onUrlClick(it.item) }
        }
    )
}

fun parseRichText(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0

        // 匹配话题 #话题#
        val topicRegex = "#([^#]+)#".toRegex()
        // 匹配 @用户
        val mentionRegex = "@([\\w\\-\\u4e00-\\u9fa5]+)".toRegex()
        // 匹配 URL
        val urlRegex = "(https?://[^\\s]+)".toRegex()
        // 匹配表情 [表情名]
        val emotionRegex = "\\[([^\\]]+)\\]".toRegex()

        // 找出所有匹配项
        val matches = mutableListOf<MatchResult>()
        matches.addAll(topicRegex.findAll(text))
        matches.addAll(mentionRegex.findAll(text))
        matches.addAll(urlRegex.findAll(text))
        matches.addAll(emotionRegex.findAll(text))

        // 按位置排序
        matches.sortBy { it.range.first }

        // 合并重叠的匹配
        val validMatches = mutableListOf<MatchResult>()
        var lastEnd = -1
        for (match in matches) {
            if (match.range.first > lastEnd) {
                validMatches.add(match)
                lastEnd = match.range.last
            }
        }

        // 构建 AnnotatedString
        for (match in validMatches) {
            // 添加匹配前的普通文本
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }

            val matchedText = match.value
            val start = length
            append(matchedText)

            when {
                matchedText.startsWith("#") -> {
                    // 话题
                    addStyle(
                        style = SpanStyle(
                            color = WeiboOrange,
                            fontWeight = FontWeight.Medium
                        ),
                        start = start,
                        end = length
                    )
                    addStringAnnotation(
                        tag = "TOPIC",
                        annotation = matchedText,
                        start = start,
                        end = length
                    )
                }
                matchedText.startsWith("@") -> {
                    // @用户
                    addStyle(
                        style = SpanStyle(
                            color = WeiboOrange,
                            fontWeight = FontWeight.Medium
                        ),
                        start = start,
                        end = length
                    )
                    addStringAnnotation(
                        tag = "MENTION",
                        annotation = matchedText.substring(1),
                        start = start,
                        end = length
                    )
                }
                matchedText.startsWith("http") -> {
                    // URL
                    addStyle(
                        style = SpanStyle(
                            color = LinkColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = start,
                        end = length
                    )
                    addStringAnnotation(
                        tag = "URL",
                        annotation = matchedText,
                        start = start,
                        end = length
                    )
                }
                matchedText.startsWith("[") -> {
                    // 表情 - 保持原样，实际应该替换为图片
                    addStyle(
                        style = SpanStyle(
                            color = Color.Gray
                        ),
                        start = start,
                        end = length
                    )
                }
            }

            currentIndex = match.range.last + 1
        }

        // 添加剩余的文本
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
