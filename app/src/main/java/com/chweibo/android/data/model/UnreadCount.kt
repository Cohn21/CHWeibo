package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 未读消息数
 */
data class UnreadCount(
    @SerializedName("status")
    val status: Int = 0,

    @SerializedName("follower")
    val follower: Int = 0,

    @SerializedName("cmt")
    val comment: Int = 0,

    @SerializedName("dm")
    val dm: Int = 0,

    @SerializedName("mention_status")
    val mentionStatus: Int = 0,

    @SerializedName("mention_cmt")
    val mentionComment: Int = 0,

    @SerializedName("group")
    val group: Int = 0,

    @SerializedName("private_group")
    val privateGroup: Int = 0,

    @SerializedName("notice")
    val notice: Int = 0,

    @SerializedName("invite")
    val invite: Int = 0,

    @SerializedName("badge")
    val badge: Int = 0,

    @SerializedName("photo")
    val photo: Int = 0
)
