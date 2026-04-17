package com.chweibo.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 数据库专用的微博实体类（使用 var 字段）
 */
@Entity(tableName = "weibo_posts")
data class WeiboPostEntity(
    @PrimaryKey
    var id: Long = 0,
    var idStr: String? = null,
    var createdAt: String = "",
    var text: String = "",
    var source: String? = null,
    var favorited: Boolean = false,
    var truncated: Boolean = false,
    var thumbnailPic: String? = null,
    var bmiddlePic: String? = null,
    var originalPic: String? = null,
    var repostsCount: Int = 0,
    var commentsCount: Int = 0,
    var attitudesCount: Int = 0,
    var cachedAt: Long = System.currentTimeMillis()
) {
    fun toWeiboPost(): WeiboPost {
        return WeiboPost(
            id = id,
            idStr = idStr,
            createdAt = createdAt,
            text = text,
            source = source,
            favorited = favorited,
            truncated = truncated,
            thumbnailPic = thumbnailPic,
            bmiddlePic = bmiddlePic,
            originalPic = originalPic,
            repostsCount = repostsCount,
            commentsCount = commentsCount,
            attitudesCount = attitudesCount
        )
    }

    companion object {
        fun fromWeiboPost(post: WeiboPost): WeiboPostEntity {
            return WeiboPostEntity(
                id = post.id,
                idStr = post.idStr,
                createdAt = post.createdAt,
                text = post.text,
                source = post.source,
                favorited = post.favorited,
                truncated = post.truncated,
                thumbnailPic = post.thumbnailPic,
                bmiddlePic = post.bmiddlePic,
                originalPic = post.originalPic,
                repostsCount = post.repostsCount,
                commentsCount = post.commentsCount,
                attitudesCount = post.attitudesCount,
                cachedAt = post.cachedAt
            )
        }
    }
}
