package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 微博表情
 */
data class Emotion(
    @SerializedName("phrase")
    val phrase: String,  // [doge]

    @SerializedName("type")
    val type: String,  // face

    @SerializedName("url")
    val url: String,  // 图片URL

    @SerializedName("hot")
    val hot: Boolean = false,

    @SerializedName("common")
    val common: Boolean = false,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("value")
    val value: String? = null,

    @SerializedName("picid")
    val picId: String? = null
) {
    companion object {
        // 常用表情分类
        const val CATEGORY_DEFAULT = "default"
        const val CATEGORY_EMOJI = "emoji"
        const val CATEGORY_LXZ = "lxz"  // 冷笑话
        const val CATEGORY_DZZ = "dzz"  // 暴走漫画
        const val CATEGORY_XHJ = "xhj"  // 小黄鸡
        const val CATEGORY_CATS = "cats"
        const val CATEGORY_BIXING = "bixing"
    }
}

/**
 * 表情分类
 */
data class EmotionCategory(
    val name: String,
    val title: String,
    val emotions: List<Emotion>
)
