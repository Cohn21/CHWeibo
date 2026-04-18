package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chweibo.android.data.model.User
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import com.chweibo.android.utils.RateLimitManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository,
    private val rateLimitManager: RateLimitManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _weibos = MutableStateFlow<List<WeiboPost>>(emptyList())
    val weibos: StateFlow<List<WeiboPost>> = _weibos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun loadUserInfo(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // 检查速率限制
            val (canCall, _) = rateLimitManager.canMakeCall("user_timeline")
            if (canCall) {
                // 加载用户信息
                weiboRepository.getUserInfo(userId)
                    .onSuccess { userInfo ->
                        _user.value = userInfo
                        _isFollowing.value = userInfo.following
                    }
                    .onFailure { e ->
                        _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "加载用户信息失败"))
                    }

                // 加载用户微博
                loadUserWeibos(userId)
            } else {
                _uiEvent.emit(UiEvent.ShowSnackbar("请求太频繁，请稍后再试"))
            }

            _isLoading.value = false
        }
    }

    private suspend fun loadUserWeibos(userId: Long) {
        weiboRepository.getUserTimeline(userId)
            .cachedIn(viewModelScope)
            .collect { pagingData ->
                // 这里需要转换为 List，实际应该使用 Paging
            }
    }

    fun toggleFollow(userId: Long) {
        viewModelScope.launch {
            val currentFollowing = _isFollowing.value
            val result = if (currentFollowing) {
                weiboRepository.unfollowUser(userId)
            } else {
                weiboRepository.followUser(userId)
            }

            result.onSuccess {
                _isFollowing.value = !currentFollowing
                _uiEvent.emit(UiEvent.ShowSnackbar(if (currentFollowing) "取消关注成功" else "关注成功"))
            }.onFailure { e ->
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "操作失败"))
            }
        }
    }
}
