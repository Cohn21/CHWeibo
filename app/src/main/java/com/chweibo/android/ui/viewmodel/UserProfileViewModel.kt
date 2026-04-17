package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chweibo.android.data.model.User
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _weibos = MutableStateFlow<List<WeiboPost>>(emptyList())
    val weibos: StateFlow<List<WeiboPost>> = _weibos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    fun loadUserInfo(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // 加载用户信息
            weiboRepository.getUserInfo(userId)
                .onSuccess { userInfo ->
                    _user.value = userInfo
                    _isFollowing.value = userInfo.following
                }
                .onFailure { e ->
                    _errorMessage.emit(e.message ?: "加载用户信息失败")
                }

            // 加载用户微博
            loadUserWeibos(userId)

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
                _errorMessage.emit(if (currentFollowing) "取消关注成功" else "关注成功")
            }.onFailure { e ->
                _errorMessage.emit(e.message ?: "操作失败")
            }
        }
    }
}