package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.model.Comment
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository
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
    val replyToComment: StateFlow<Comment?> = _replyToComment.asStateFlow()

    private var currentPage = 1

    fun loadWeiboAndComments(weiboId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // 加载微博详情
            weiboRepository.getWeiboDetail(weiboId)
                .onSuccess { post ->
                    _weibo.value = post
                }

            // 加载评论
            loadComments(weiboId)

            _isLoading.value = false
        }
    }

    private suspend fun loadComments(weiboId: Long) {
        weiboRepository.getComments(weiboId, currentPage)
            .onSuccess { response ->
                _comments.value = response.comments
            }
            .onFailure {
                // 处理错误
            }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun replyTo(comment: Comment) {
        _replyToComment.value = comment
        _commentText.value = "回复 @${comment.user?.screenName}: "
    }

    fun clearReply() {
        _replyToComment.value = null
        _commentText.value = ""
    }

    fun postComment(weiboId: Long) {
        viewModelScope.launch {
            val text = _commentText.value.trim()
            if (text.isEmpty()) return@launch

            val replyComment = _replyToComment.value

            val result = if (replyComment != null) {
                // 回复评论
                weiboRepository.replyComment(weiboId, replyComment.id, text)
            } else {
                // 发表评论
                weiboRepository.createComment(weiboId, text)
            }

            result.onSuccess { newComment ->
                _comments.value = listOf(newComment) + _comments.value
                _commentText.value = ""
                _replyToComment.value = null
            }.onFailure {
                // 显示错误
            }
        }
    }

    fun loadMoreComments(weiboId: Long) {
        viewModelScope.launch {
            currentPage++
            weiboRepository.getComments(weiboId, currentPage)
                .onSuccess { response ->
                    _comments.value = _comments.value + response.comments
                }
        }
    }
}