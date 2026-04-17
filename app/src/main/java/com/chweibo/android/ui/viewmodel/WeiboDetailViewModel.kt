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
class WeiboDetailViewModel @Inject constructor(
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

    fun loadWeiboDetail(weiboId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // 加载微博详情
            weiboRepository.getWeiboDetail(weiboId)
                .onSuccess { post ->
                    _weibo.value = post
                }
                .onFailure {
                    // 处理错误
                }

            // 加载评论
            weiboRepository.getComments(weiboId)
                .onSuccess { response ->
                    _comments.value = response.comments
                }

            _isLoading.value = false
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun replyTo(comment: Comment) {
        _replyToComment.value = comment
        _commentText.value = "回复 @${comment.user?.screenName}: "
    }

    fun postComment() {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch
            val text = _commentText.value.trim()
            if (text.isEmpty()) return@launch

            val replyComment = _replyToComment.value

            val result = if (replyComment != null) {
                weiboRepository.replyComment(weiboId, replyComment.id, text)
            } else {
                weiboRepository.createComment(weiboId, text)
            }

            result.onSuccess { newComment ->
                _comments.value = listOf(newComment) + _comments.value
                _commentText.value = ""
                _replyToComment.value = null

                // 更新评论数
                _weibo.value = _weibo.value?.copy(
                    commentsCount = (_weibo.value?.commentsCount ?: 0) + 1
                )
            }.onFailure {
                // 显示错误
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch
            val isLiked = _weibo.value?.favorited ?: false

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
            }
        }
    }

    fun repost(content: String?) {
        viewModelScope.launch {
            val weiboId = _weibo.value?.id ?: return@launch

            weiboRepository.repostWeibo(weiboId, content)
                .onSuccess {
                    // 转发成功
                    _weibo.value = _weibo.value?.copy(
                        repostsCount = (_weibo.value?.repostsCount ?: 0) + 1
                    )
                }.onFailure {
                    // 显示错误
                }
        }
    }
}