package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 微博评论
 */
data class Comment(
    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("id")
    val id: Long,

    @SerializedName("idstr")
    val idStr: String? = null,

    @SerializedName("text")
    val text: String,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("mid")
    val mid: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("status")
    val status: WeiboPost? = null,

    @SerializedName("reply_comment")
    val replyComment: Comment? = null,

    @SerializedName("floor_num")
    val floorNum: Int = 0
)

/**
 * 评论列表响应
 */
data class CommentResponse(
    @SerializedName("comments")
    val comments: List<Comment> = emptyList(),

    @SerializedName("previous_cursor")
    val previousCursor: Long = 0,

    @SerializedName("next_cursor")
    val nextCursor: Long = 0,

    @SerializedName("total_number")
    val totalNumber: Int = 0,

    @SerializedName("hasvisible")
    val hasVisible: Boolean = false
)
