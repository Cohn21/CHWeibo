package com.chweibo.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson

@Entity(tableName = "weibo_posts")
data class WeiboPostEntity(
    @PrimaryKey
    var id: Long = 0,
    var idStr: String? = null,
    var contentJson: String = "",
    var cachedAt: Long = System.currentTimeMillis()
) {
    fun toWeiboPost(): WeiboPost {
        return Gson().fromJson(contentJson, WeiboPost::class.java)
    }

    companion object {
        fun fromWeiboPost(post: WeiboPost): WeiboPostEntity {
            return WeiboPostEntity(
                id = post.id,
                idStr = post.idStr,
                contentJson = Gson().toJson(post),
                cachedAt = post.cachedAt
            )
        }
    }
}
