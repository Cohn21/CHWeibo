package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 微博帖子（网络模型）
 */
data class WeiboPost(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("idstr")
    val idStr: String? = null,

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("text")
    val text: String = "",

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("favorited")
    val favorited: Boolean = false,

    @SerializedName("truncated")
    val truncated: Boolean = false,

    @SerializedName("in_reply_to_status_id")
    val inReplyToStatusId: String? = null,

    @SerializedName("in_reply_to_user_id")
    val inReplyToUserId: String? = null,

    @SerializedName("in_reply_to_screen_name")
    val inReplyToScreenName: String? = null,

    @SerializedName("thumbnail_pic")
    val thumbnailPic: String? = null,

    @SerializedName("bmiddle_pic")
    val bmiddlePic: String? = null,

    @SerializedName("original_pic")
    val originalPic: String? = null,

    @SerializedName("geo")
    val geo: Geo? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("retweeted_status")
    val retweetedStatus: WeiboPost? = null,

    @SerializedName("reposts_count")
    val repostsCount: Int = 0,

    @SerializedName("comments_count")
    val commentsCount: Int = 0,

    @SerializedName("attitudes_count")
    val attitudesCount: Int = 0,

    @SerializedName("mlevel")
    val mlevel: Int = 0,

    @SerializedName("visible")
    val visible: Visible? = null,

    @SerializedName("pic_ids")
    val picIds: List<String>? = null,

    @SerializedName("pic_infos")
    val picInfos: Map<String, PicInfo>? = null,

    @SerializedName("pic_urls")
    val picUrls: List<PicUrl>? = null,

    @SerializedName("isLongText")
    val isLongText: Boolean = false,

    @SerializedName("card")
    val card: Card? = null,

    @SerializedName("video_url")
    val videoUrl: String? = null,

    @SerializedName("video_cover")
    val videoCoverUrl: String? = null,

    // 本地字段
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun getSourceText(): String {
        return source?.let {
            val matcher = android.util.Patterns.WEB_URL.matcher(it)
            if (matcher.find()) {
                val start = it.indexOf(">") + 1
                val end = it.indexOf("</")
                if (start > 0 && end > start) {
                    return it.substring(start, end)
                }
            }
            it
        } ?: ""
    }

    fun getAllPics(): List<String> {
        return picUrls?.map { it.thumbnailPic?.replace("thumbnail", "large") ?: "" }
            ?: picInfos?.values?.map { it.original?.url ?: "" }
            ?: emptyList()
    }

    fun getThumbnailPics(): List<String> {
        return picUrls?.map { it.thumbnailPic ?: "" }
            ?: picInfos?.values?.map { it.thumbnail?.url ?: "" }
            ?: emptyList()
    }

    fun hasVideo(): Boolean = !videoUrl.isNullOrEmpty()
}

data class Geo(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("coordinates")
    val coordinates: List<Double>? = null
)

data class Visible(
    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("list_id")
    val listId: Int = 0
)

data class PicUrl(
    @SerializedName("thumbnail_pic")
    val thumbnailPic: String? = null
)

data class PicInfo(
    @SerializedName("thumbnail")
    val thumbnail: PicDetail? = null,

    @SerializedName("bmiddle")
    val bmiddle: PicDetail? = null,

    @SerializedName("large")
    val large: PicDetail? = null,

    @SerializedName("original")
    val original: PicDetail? = null
)

data class PicDetail(
    @SerializedName("url")
    val url: String? = null,

    @SerializedName("width")
    val width: Int = 0,

    @SerializedName("height")
    val height: Int = 0,

    @SerializedName("type")
    val type: String? = null
)

data class Card(
    @SerializedName("card_type")
    val cardType: Int = 0,

    @SerializedName("itemid")
    val itemId: String? = null,

    @SerializedName("scheme")
    val scheme: String? = null
)

/**
 * 微博列表响应
 */
data class WeiboTimelineResponse(
    @SerializedName("statuses")
    val statuses: List<WeiboPost> = emptyList(),

    @SerializedName("previous_cursor")
    val previousCursor: Long = 0,

    @SerializedName("next_cursor")
    val nextCursor: Long = 0,

    @SerializedName("total_number")
    val totalNumber: Int = 0,

    @SerializedName("since_id")
    val sinceId: Long = 0,

    @SerializedName("max_id")
    val maxId: Long = 0,

    @SerializedName("has_unread")
    val hasUnread: Int = 0
)
