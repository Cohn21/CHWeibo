package com.chweibo.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * 微博 OAuth2 Access Token
 */
data class AccessToken(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("remind_in")
    val remindIn: String? = null,

    @SerializedName("expires_in")
    val expiresIn: Long = 0,

    @SerializedName("uid")
    val uid: String? = null,

    @SerializedName("isRealName")
    val isRealName: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    // 本地计算的过期时间
    val expiresAt: Long = System.currentTimeMillis() + (expiresIn * 1000)
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }
}

/**
 * Token 信息响应
 */
data class TokenInfo(
    @SerializedName("uid")
    val uid: Long = 0,

    @SerializedName("appkey")
    val appKey: String? = null,

    @SerializedName("scope")
    val scope: String? = null,

    @SerializedName("create_at")
    val createAt: Long = 0,

    @SerializedName("expire_in")
    val expireIn: Long = 0
)

/**
 * OAuth2 授权请求参数
 */
data class AuthConfig(
    val clientId: String,
    val redirectUri: String = "https://api.weibo.com/oauth2/default.html",
    val scope: String = "email,direct_messages_read,direct_messages_write," +
            "friendships_groups_read,friendships_groups_write," +
            "statuses_to_me_read,follow_app_official_microblog," +
            "invitation_write",
    val display: String = "mobile",
    val forcelogin: Boolean = false,
    val language: String = "zh"
) {
    fun getAuthorizeUrl(): String {
        return "https://api.weibo.com/oauth2/authorize?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "scope=$scope&" +
                "display=$display&" +
                "forcelogin=${if (forcelogin) "true" else "false"}"
    }
}
