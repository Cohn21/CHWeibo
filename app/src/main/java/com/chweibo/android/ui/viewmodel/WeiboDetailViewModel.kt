package com.chweibo.android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.model.Comment
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import com.chweibo.android.utils.RateLimitManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeiboDetailViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository,
    private val rateLimitManager: RateLimitManager
) : ViewModel() {

    private val _weibo = MutableStateFlow<WeiboPost?>(null)
    val weibo: StateFlow<WeiboPost?> = _weibo.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _replyToComment = MutableStateFlow<Comment?>(null)

    private val _uiEvent = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    companion object {
        private const val TAG = "WeiboDetailViewModel"
    }

    fun loadWeiboDetail(weiboId: String) {
        Log.d(TAG, "Loading weibo detail for id: $weiboId")

        if (weiboId.isBlank()) {
            Log.e(TAG, "Invalid weiboId: $weiboId")
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowSnackbar("Invalid weibo id: $weiboId"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            weiboRepository.getWeiboDetail(weiboId)
                .onSuccess { post ->
                    Log.d(TAG, "Loaded weibo: ${post.id}, text: ${post.text.take(30)}")
                    _weibo.value = post
                    loadComments(post.id)
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load weibo: ${e.message}", e)
                    val cachedWeibo = weiboRepository.getCachedWeibo(weiboId)
                    if (e.message?.contains("20112") == true && cachedWeibo != null) {
                        _weibo.value = cachedWeibo
                        _comments.value = emptyList()
                        _uiEvent.emit(UiEvent.ShowSnackbar("Detail API denied access. Showing cached timeline content."))
                    } else {
                        _weibo.value = null
                        _comments.value = emptyList()
                        handleApiError(e, "Failed to load weibo detail")
                    }
                }

            _isLoading.value = false
        }
    }

    private suspend fun loadComments(weiboId: Long) {
        val (canCallComments, _) = rateLimitManager.canMakeCall("comments")
        if (canCallComments) {
            rateLimitManager.recordCall("comments")
            weiboRepository.getComments(weiboId)
                .onSuccess { response ->
                    _comments.value = response.comments
                }
                .onFailure { e ->
                    handleApiError(e, "Failed to load comments")
                }
        } else {
            _uiEvent.emit(UiEvent.ShowSnackbar("Comments are rate limited: ${rateLimitManager.getWaitMessage("comments")}"))
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun replyTo(comment: Comment) {
        _replyToComment.value = comment
        _commentText.value = "Reply @${comment.user?.screenName}: "
    }

    fun postComment() {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch
            val text = _commentText.value.trim()
            if (text.isEmpty()) return@launch

            val (canCall, _) = rateLimitManager.canMakeCall("comments")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("comments")))
                return@launch
            }

            val replyComment = _replyToComment.value

            rateLimitManager.recordCall("comments")
            val result = if (replyComment != null) {
                weiboRepository.replyComment(weiboId, replyComment.id, text)
            } else {
                weiboRepository.createComment(weiboId, text)
            }

            result.onSuccess { newComment ->
                _comments.value = listOf(newComment) + _comments.value
                _commentText.value = ""
                _replyToComment.value = null
                _weibo.value = _weibo.value?.copy(
                    commentsCount = (_weibo.value?.commentsCount ?: 0) + 1
                )
            }.onFailure { e ->
                handleApiError(e, "Failed to post comment")
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch
            val isLiked = _weibo.value?.favorited ?: false

            val (canCall, _) = rateLimitManager.canMakeCall("like")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("like")))
                return@launch
            }

            rateLimitManager.recordCall("like")
            val result = if (isLiked) {
                weiboRepository.destroyAttitude(weiboId)
            } else {
                weiboRepository.createAttitude(weiboId)
            }

            result.onSuccess {
                _weibo.value = _weibo.value?.copy(
                    favorited = !isLiked,
                    attitudesCount = if (isLiked) {
                        (_weibo.value?.attitudesCount ?: 1) - 1
                    } else {
                        (_weibo.value?.attitudesCount ?: 0) + 1
                    }
                )
            }.onFailure { e ->
                handleApiError(e, if (isLiked) "Failed to unlike weibo" else "Failed to like weibo")
            }
        }
    }

    fun repost(content: String?) {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch

            val (canCall, _) = rateLimitManager.canMakeCall("post")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("post")))
                return@launch
            }

            rateLimitManager.recordCall("post")
            weiboRepository.repostWeibo(weiboId, content)
                .onSuccess {
                    _weibo.value = _weibo.value?.copy(
                        repostsCount = (_weibo.value?.repostsCount ?: 0) + 1
                    )
                    _uiEvent.emit(UiEvent.ShowSnackbar("Repost succeeded"))
                }.onFailure { e ->
                    handleApiError(e, "Failed to repost weibo")
                }
        }
    }

    private suspend fun handleApiError(e: Throwable, defaultMsg: String) {
        Log.e(TAG, "API Error: ${e.javaClass.simpleName}: ${e.message}", e)
        val msg = when {
            e.message?.contains("10023") == true -> {
                rateLimitManager.setCooldown(true)
                "Request too frequent, please try again later"
            }
            e.message?.contains("10022") == true -> "API quota exceeded"
            e.message?.contains("10006") == true -> "Login required"
            e.message?.contains("20101") == true -> "Weibo not found or deleted"
            e.message?.contains("20112") == true -> "Permission denied for this weibo detail"
            e.message?.contains("20003") == true -> "User not found"
            else -> "$defaultMsg: ${e.message}"
        }
        _uiEvent.emit(UiEvent.ShowSnackbar(msg))
    }
}
