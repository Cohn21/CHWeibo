package com.chweibo.android.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.chweibo.android.data.api.WeiboApiService
import com.chweibo.android.data.local.dao.WeiboDao
import com.chweibo.android.data.model.*
import com.chweibo.android.data.model.UnreadCount
import com.chweibo.android.data.paging.WeiboPagingSource
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeiboRepository @Inject constructor(
    private val apiService: WeiboApiService,
    private val weiboDao: WeiboDao
) {

    // ==================== 时间线 ====================

    fun getHomeTimeline(): Flow<PagingData<WeiboPost>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                WeiboPagingSource { page, sinceId, maxId ->
                    apiService.getHomeTimeline(
                        sinceId = sinceId,
                        maxId = maxId,
                        count = page,
                        page = 1
                    )
                }
            }
        ).flow
    }

    fun getUserTimeline(uid: Long): Flow<PagingData<WeiboPost>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                WeiboPagingSource { page, sinceId, maxId ->
                    apiService.getUserTimeline(
                        uid = uid,
                        sinceId = sinceId,
                        maxId = maxId,
                        count = page
                    )
                }
            }
        ).flow
    }

    suspend fun getHomeTimelineSingle(sinceId: Long? = null, maxId: Long? = null): Result<WeiboTimelineResponse> {
        return try {
            val response = apiService.getHomeTimeline(sinceId = sinceId, maxId = maxId, count = 20)
            if (response.isSuccessful && response.body() != null) {
                // 缓存到本地
                response.body()?.statuses?.let { posts ->
                    val entities = posts.map { WeiboPostEntity.fromWeiboPost(it) }
                    weiboDao.insertPosts(entities)
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 发布微博 ====================

    suspend fun postWeibo(content: String, visible: Int = 0): Result<WeiboPost> {
        return try {
            val response = apiService.postWeibo(
                status = content,
                visible = visible
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postWeiboWithImage(content: String, imageFile: File, visible: Int = 0): Result<WeiboPost> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("pic", imageFile.name, requestFile)
            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val visibleBody = visible.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.postWeiboWithImage(
                status = contentBody,
                pic = imagePart,
                visible = visibleBody
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWeibo(id: Long): Result<Unit> {
        return try {
            val response = apiService.deleteWeibo(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeiboDetail(id: Long): Result<WeiboPost> {
        return try {
            val response = apiService.getWeiboDetail(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 转发 ====================

    suspend fun repostWeibo(id: Long, content: String? = null, isComment: Boolean = false): Result<WeiboPost> {
        return try {
            val response = apiService.repostWeibo(
                id = id,
                status = content,
                isComment = if (isComment) 1 else 0
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 评论 ====================

    suspend fun getComments(id: Long, page: Int = 1): Result<CommentResponse> {
        return try {
            val response = apiService.getComments(id = id, page = page, count = 20)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(weiboId: Long, content: String, commentOri: Boolean = false): Result<Comment> {
        return try {
            val response = apiService.createComment(
                comment = content,
                id = weiboId,
                commentOri = if (commentOri) 1 else 0
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyComment(weiboId: Long, commentId: Long, content: String): Result<Comment> {
        return try {
            val response = apiService.replyComment(
                commentId = commentId,
                weiboId = weiboId,
                comment = content
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 点赞 ====================

    suspend fun createAttitude(weiboId: Long): Result<Unit> {
        return try {
            val response = apiService.createAttitude(id = weiboId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun destroyAttitude(weiboId: Long): Result<Unit> {
        return try {
            val response = apiService.destroyAttitude(id = weiboId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 表情 ====================

    suspend fun getEmotions(): Result<List<Emotion>> {
        return try {
            val response = apiService.getEmotions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 未读消息 ====================

    suspend fun getUnreadCount(uid: Long? = null): Result<UnreadCount> {
        return try {
            val response = apiService.getUnreadCount(uid = uid)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 用户 ====================

    suspend fun getUserInfo(uid: Long): Result<User> {
        return try {
            val response = apiService.getUserInfo(uid = uid)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(uid: Long): Result<User> {
        return try {
            val response = apiService.followUser(uid = uid)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(uid: Long): Result<User> {
        return try {
            val response = apiService.unfollowUser(uid = uid)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 缓存操作 ====================

    fun getCachedPosts(): Flow<List<WeiboPost>> {
        return weiboDao.getAllPosts().map { entities ->
            entities.map { it.toWeiboPost() }
        }
    }

    suspend fun cachePosts(posts: List<WeiboPost>) {
        val entities = posts.map { WeiboPostEntity.fromWeiboPost(it) }
        weiboDao.insertPosts(entities)
    }

    suspend fun clearCache() {
        weiboDao.deleteAllPosts()
    }
}
