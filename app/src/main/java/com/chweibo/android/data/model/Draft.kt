package com.chweibo.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 微博草稿
 */
@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    // 草稿内容
    var content: String = "",

    // 选中的图片路径（用逗号分隔存储）
    var imageUris: String = "",

    // 是否转发
    var isRepost: Boolean = false,

    // 原微博ID（如果是转发）
    var sourceWeiboId: Long? = null,

    // 可见性: 0-公开, 1-仅自己, 2-好友圈
    var visible: Int = 0,

    // 创建时间
    var createdAt: Long = System.currentTimeMillis(),

    // 最后修改时间
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun getImageUriList(): List<String> {
        return if (imageUris.isEmpty()) emptyList()
        else imageUris.split(",")
    }

    fun setImageUriList(uris: List<String>) {
        imageUris = uris.joinToString(",")
    }
}
