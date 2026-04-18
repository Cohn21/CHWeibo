package com.chweibo.android.data.api

import com.chweibo.android.BuildConfig
import com.chweibo.android.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface WeiboApiService {

    // ==================== OAuth2 认证 ====================

    @POST("oauth2/access_token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<AccessToken>

    @POST("oauth2/get_token_info")
    @FormUrlEncoded
    suspend fun getTokenInfo(
        @Field("access_token") accessToken: String
    ): Response<TokenInfo>

    @POST("oauth2/revokeoauth2")
    @FormUrlEncoded
    suspend fun revokeToken(
        @Field("access_token") accessToken: String
    ): Response<Map<String, Any>>

    @POST("oauth2/access_token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String = BuildConfig.WEIBO_APP_KEY,
        @Field("client_secret") clientSecret: String = BuildConfig.WEIBO_CLIENT_SECRET
    ): AccessToken

    // ==================== 用户信息 ====================

    @GET("users/show.json")
    suspend fun getUserInfo(
        @Query("uid") uid: Long? = null,
        @Query("screen_name") screenName: String? = null
    ): Response<User>

    @GET("users/domain_show.json")
    suspend fun getUserByDomain(
        @Query("domain") domain: String
    ): Response<User>

    @GET("users/counts.json")
    suspend fun getUserCounts(
        @Query("uids") uids: String
    ): Response<List<UserCount>>

    // ==================== 微博时间线 ====================

    @GET("statuses/home_timeline.json")
    suspend fun getHomeTimeline(
        @Query("since_id") sinceId: Long? = null,
        @Query("max_id") maxId: Long? = null,
        @Query("count") count: Int = 20,
        @Query("page") page: Int = 1,
        @Query("base_app") baseApp: Int = 0,
        @Query("feature") feature: Int = 0,
        @Query("trim_user") trimUser: Int = 0
    ): Response<WeiboTimelineResponse>

    @GET("statuses/user_timeline.json")
    suspend fun getUserTimeline(
        @Query("uid") uid: Long? = null,
        @Query("screen_name") screenName: String? = null,
        @Query("since_id") sinceId: Long? = null,
        @Query("max_id") maxId: Long? = null,
        @Query("count") count: Int = 20,
        @Query("page") page: Int = 1,
        @Query("base_app") baseApp: Int = 0,
        @Query("feature") feature: Int = 0,
        @Query("trim_user") trimUser: Int = 0
    ): Response<WeiboTimelineResponse>

    @GET("statuses/public_timeline.json")
    suspend fun getPublicTimeline(
        @Query("count") count: Int = 20,
        @Query("page") page: Int = 1,
        @Query("base_app") baseApp: Int = 0
    ): Response<WeiboTimelineResponse>

    // ==================== 微博操作 ====================

    @POST("statuses/update.json")
    @FormUrlEncoded
    suspend fun postWeibo(
        @Field("status") status: String,
        @Field("visible") visible: Int? = null,
        @Field("list_id") listId: String? = null,
        @Field("lat") lat: Float? = null,
        @Field("long") long: Float? = null,
        @Field("annotations") annotations: String? = null
    ): Response<WeiboPost>

    @POST("statuses/upload.json")
    @Multipart
    suspend fun postWeiboWithImage(
        @Part("status") status: RequestBody,
        @Part pic: MultipartBody.Part,
        @Part("visible") visible: RequestBody? = null,
        @Part("lat") lat: RequestBody? = null,
        @Part("long") long: RequestBody? = null
    ): Response<WeiboPost>

    @POST("statuses/upload_url_text.json")
    @FormUrlEncoded
    suspend fun postWeiboWithUrl(
        @Field("status") status: String,
        @Field("url") url: String,
        @Field("visible") visible: Int? = null,
        @Field("pic_id") picId: String? = null
    ): Response<WeiboPost>

    @POST("statuses/destroy/{id}.json")
    suspend fun deleteWeibo(
        @Path("id") id: Long
    ): Response<WeiboPost>

    @GET("statuses/show.json")
    suspend fun getWeiboDetail(
        @Query("id") id: String
    ): Response<WeiboPost>

    // ==================== 转发 ====================

    @POST("statuses/repost.json")
    @FormUrlEncoded
    suspend fun repostWeibo(
        @Field("id") id: Long,
        @Field("status") status: String? = null,
        @Field("is_comment") isComment: Int = 0
    ): Response<WeiboPost>

    // ==================== 评论 ====================

    @GET("comments/show.json")
    suspend fun getComments(
        @Query("id") id: Long,
        @Query("since_id") sinceId: Long? = null,
        @Query("max_id") maxId: Long? = null,
        @Query("count") count: Int = 20,
        @Query("page") page: Int = 1,
        @Query("filter_by_author") filterByAuthor: Int = 0
    ): Response<CommentResponse>

    @POST("comments/create.json")
    @FormUrlEncoded
    suspend fun createComment(
        @Field("comment") comment: String,
        @Field("id") id: Long,
        @Field("comment_ori") commentOri: Int = 0
    ): Response<Comment>

    @POST("comments/destroy/{cid}.json")
    suspend fun deleteComment(
        @Path("cid") commentId: Long
    ): Response<Comment>

    @POST("comments/reply.json")
    @FormUrlEncoded
    suspend fun replyComment(
        @Field("cid") commentId: Long,
        @Field("id") weiboId: Long,
        @Field("comment") comment: String,
        @Field("without_mention") withoutMention: Int = 0,
        @Field("comment_ori") commentOri: Int = 0
    ): Response<Comment>

    // ==================== 点赞 ====================

    @POST("attitudes/create.json")
    @FormUrlEncoded
    suspend fun createAttitude(
        @Field("id") id: Long,
        @Field("attitude") attitude: String = "face"
    ): Response<Map<String, Any>>

    @POST("attitudes/destroy.json")
    @FormUrlEncoded
    suspend fun destroyAttitude(
        @Field("id") id: Long
    ): Response<Map<String, Any>>

    // ==================== 表情 ====================

    @GET("emotions.json")
    suspend fun getEmotions(
        @Query("type") type: String? = null,
        @Query("language") language: String = "zh_cn"
    ): Response<List<Emotion>>

    // ==================== 搜索 ====================

    @GET("search/topics.json")
    suspend fun searchTopics(
        @Query("q") query: String,
        @Query("count") count: Int = 20,
        @Query("page") page: Int = 1
    ): Response<Map<String, Any>>

    // ==================== 未读消息 ====================

    @GET("remind/unread_count.json")
    suspend fun getUnreadCount(
        @Query("uid") uid: Long? = null
    ): Response<com.chweibo.android.data.model.UnreadCount>

    // ==================== 好友关注 ====================

    @GET("friendships/friends.json")
    suspend fun getFriends(
        @Query("uid") uid: Long,
        @Query("count") count: Int = 50,
        @Query("cursor") cursor: Int = 0,
        @Query("trim_status") trimStatus: Int = 1
    ): Response<FriendsResponse>

    @GET("friendships/followers.json")
    suspend fun getFollowers(
        @Query("uid") uid: Long,
        @Query("count") count: Int = 50,
        @Query("cursor") cursor: Int = 0
    ): Response<FriendsResponse>

    @POST("friendships/create.json")
    @FormUrlEncoded
    suspend fun followUser(
        @Field("uid") uid: Long? = null,
        @Field("screen_name") screenName: String? = null
    ): Response<User>

    @POST("friendships/destroy.json")
    @FormUrlEncoded
    suspend fun unfollowUser(
        @Field("uid") uid: Long? = null,
        @Field("screen_name") screenName: String? = null
    ): Response<User>

    // ==================== 账号限制查询 ====================

    @GET("account/rate_limit_status.json")
    suspend fun getRateLimitStatus(): Response<RateLimitStatus>
}

data class RateLimitStatus(
    val ip_limit: Int = 0,
    val limit_time_unit: String = "",
    val remaining_ip_hits: Int = 0,
    val remaining_user_hits: Int = 0,
    val reset_time: String = "",
    val reset_time_in_seconds: Int = 0,
    val user_limit: Int = 0
)

data class UserCount(
    val id: Long,
    val followers_count: Int,
    val friends_count: Int,
    val statuses_count: Int
)


data class FriendsResponse(
    val users: List<User> = emptyList(),
    val next_cursor: Long = 0,
    val previous_cursor: Long = 0,
    val total_number: Int = 0
)
