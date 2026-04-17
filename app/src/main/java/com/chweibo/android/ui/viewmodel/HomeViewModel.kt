package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chweibo.android.data.model.WeiboPost
import com.chweibo.android.data.repository.WeiboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _timeline = MutableStateFlow<PagingData<WeiboPost>>(PagingData.empty())
    val timeline: StateFlow<PagingData<WeiboPost>> = _timeline.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    init {
        loadTimeline()
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
            _refreshing.value = true
            try {
                // 重新加载数据
                loadTimeline()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "刷新失败")
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun likeWeibo(weiboId: Long) {
        viewModelScope.launch {
            weiboRepository.createAttitude(weiboId)
                .onFailure { e ->
                    _errorMessage.emit(e.message ?: "点赞失败")
                }
        }
    }

    fun unlikeWeibo(weiboId: Long) {
        viewModelScope.launch {
            weiboRepository.destroyAttitude(weiboId)
                .onFailure { e ->
                    _errorMessage.emit(e.message ?: "取消点赞失败")
                }
        }
    }

    fun repostWeibo(weiboId: Long, content: String?) {
        viewModelScope.launch {
            weiboRepository.repostWeibo(weiboId, content)
                .onSuccess {
                    _errorMessage.emit("转发成功")
                }
                .onFailure { e ->
                    _errorMessage.emit(e.message ?: "转发失败")
                }
        }
    }
}
