package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 微博用户信息
 */
data class User(
    @SerializedName("id")
    val id: Long,

    @SerializedName("idstr")
    val idStr: String? = null,

    @SerializedName("screen_name")
    val screenName: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("province")
    val province: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,

    @SerializedName("profile_url")
    val profileUrl: String? = null,

    @SerializedName("domain")
    val domain: String? = null,

    @SerializedName("weihao")
    val weihao: String? = null,

    @SerializedName("gender")
    val gender: String? = null,  // m:男, f:女, n:未知

    @SerializedName("followers_count")
    val followersCount: Int = 0,

    @SerializedName("friends_count")
    val friendsCount: Int = 0,

    @SerializedName("statuses_count")
    val statusesCount: Int = 0,

    @SerializedName("favourites_count")
    val favouritesCount: Int = 0,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("following")
    val following: Boolean = false,

    @SerializedName("allow_all_act_msg")
    val allowAllActMsg: Boolean = false,

    @SerializedName("geo_enabled")
    val geoEnabled: Boolean = false,

    @SerializedName("verified")
    val verified: Boolean = false,

    @SerializedName("verified_type")
    val verifiedType: Int = -1,

    @SerializedName("remark")
    val remark: String? = null,

    @SerializedName("status")
    val status: WeiboPost? = null,

    @SerializedName("allow_all_comment")
    val allowAllComment: Boolean = true,

    @SerializedName("avatar_large")
    val avatarLarge: String? = null,

    @SerializedName("avatar_hd")
    val avatarHd: String? = null,

    @SerializedName("verified_reason")
    val verifiedReason: String? = null,

    @SerializedName("follow_me")
    val followMe: Boolean = false,

    @SerializedName("online_status")
    val onlineStatus: Int = 0,

    @SerializedName("bi_followers_count")
    val biFollowersCount: Int = 0
) {
    fun getDisplayName(): String = screenName

    fun getVerifiedIcon(): Int {
        return when (verifiedType) {
            0 -> 0 // 个人认证
            1, 2, 3, 4, 5, 6, 7 -> 1 // 机构认证
            else -> -1
        }
    }
}
