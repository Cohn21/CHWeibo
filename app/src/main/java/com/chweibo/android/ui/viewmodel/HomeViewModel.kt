package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.AuthRepository
import com.chweibo.android.data.repository.WeiboRepository
import com.chweibo.android.utils.RateLimitManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository,
    private val authRepository: AuthRepository,
    private val rateLimitManager: RateLimitManager
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _timeline = MutableStateFlow<PagingData<WeiboPost>>(PagingData.empty())
    val timeline: StateFlow<PagingData<WeiboPost>> = _timeline.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 防抖动：记录上次刷新时间
    private var lastRefreshTime = 0L
    private val minRefreshInterval = 3000L // 最少3秒间隔

    init {
        loadTimeline()
        // 定期更新速率限制状态
        fetchRateLimitStatus()
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            weiboRepository.getHomeTimeline()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _timeline.value = pagingData
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // 使用 RateLimitManager 检查是否可以调用
            val (canCall, waitTime) = rateLimitManager.canMakeCall("home_timeline")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("home_timeline")))
                return@launch
            }

            // 检查刷新间隔，防止频繁请求
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRefreshTime < minRefreshInterval) {
                _uiEvent.emit(UiEvent.ShowSnackbar("操作太频繁，请稍后再试"))
                return@launch
            }

            _refreshing.value = true
            lastRefreshTime = currentTime

            try {
                rateLimitManager.recordCall("home_timeline")
                loadTimeline()
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("10023") == true -> {
                        rateLimitManager.setCooldown(true)
                        "请求太频繁，请稍后再试"
                    }
                    e.message?.contains("10022") == true -> "接口请求次数已用完"
                    else -> e.message ?: "刷新失败"
                }
                _uiEvent.emit(UiEvent.ShowSnackbar(errorMsg))
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun likeWeibo(weiboId: Long) {
        viewModelScope.launch {
            val (canCall, _) = rateLimitManager.canMakeCall("like")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("like")))
                return@launch
            }

            rateLimitManager.recordCall("like")
            weiboRepository.createAttitude(weiboId)
                .onFailure { e ->
                    handleApiError(e, "点赞失败")
                }
        }
    }

    fun unlikeWeibo(weiboId: Long) {
        viewModelScope.launch {
            val (canCall, _) = rateLimitManager.canMakeCall("like")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("like")))
                return@launch
            }

            rateLimitManager.recordCall("like")
            weiboRepository.destroyAttitude(weiboId)
                .onFailure { e ->
                    handleApiError(e, "取消点赞失败")
                }
        }
    }

    fun repostWeibo(weiboId: Long, content: String?) {
        viewModelScope.launch {
            val (canCall, _) = rateLimitManager.canMakeCall("post")
            if (!canCall) {
                _uiEvent.emit(UiEvent.ShowSnackbar(rateLimitManager.getWaitMessage("post")))
                return@launch
            }

            rateLimitManager.recordCall("post")
            weiboRepository.repostWeibo(weiboId, content)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSnackbar("转发成功"))
                }
                .onFailure { e ->
                    handleApiError(e, "转发失败")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiEvent.emit(UiEvent.Navigate("login"))
        }
    }

    private fun fetchRateLimitStatus() {
        viewModelScope.launch {
            weiboRepository.getRateLimitStatus()
                .onSuccess { status ->
                    rateLimitManager.updateRateLimitStatus(status)
                }
        }
    }

    private suspend fun handleApiError(e: Throwable, defaultMsg: String) {
        val msg = when {
            e.message?.contains("10023") == true -> {
                rateLimitManager.setCooldown(true)
                "请求太频繁，请稍后再试"
            }
            e.message?.contains("10022") == true -> "接口请求次数已用完"
            else -> e.message ?: defaultMsg
        }
        _uiEvent.emit(UiEvent.ShowSnackbar(msg))
    }
}
