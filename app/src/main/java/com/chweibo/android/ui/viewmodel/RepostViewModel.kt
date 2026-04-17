package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepostViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository
) : ViewModel() {

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _originalWeibo = MutableStateFlow<WeiboPost?>(null)
    val originalWeibo: StateFlow<WeiboPost?> = _originalWeibo.asStateFlow()

    private val _isReposting = MutableStateFlow(false)
    val isReposting: StateFlow<Boolean> = _isReposting.asStateFlow()

    private val _isComment = MutableStateFlow(false)
    val isComment: StateFlow<Boolean> = _isComment.asStateFlow()

    val canRepost: StateFlow<Boolean> = combine(
        content,
        isReposting
    ) { content, isReposting ->
        !isReposting
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun loadOriginalWeibo(weiboId: Long) {
        viewModelScope.launch {
            weiboRepository.getWeiboDetail(weiboId)
                .onSuccess { weibo ->
                    _originalWeibo.value = weibo
                }
        }
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun setIsComment(value: Boolean) {
        _isComment.value = value
    }

    fun repost(weiboId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isReposting.value = true

            weiboRepository.repostWeibo(
                id = weiboId,
                content = _content.value.takeIf { it.isNotBlank() },
                isComment = _isComment.value
            ).onSuccess {
                _isReposting.value = false
                onSuccess()
            }.onFailure {
                _isReposting.value = false
            }
        }
    }
}