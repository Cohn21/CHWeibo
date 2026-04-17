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
class SearchViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<WeiboPost>>(emptyList())
    val searchResults: StateFlow<List<WeiboPost>> = _searchResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hotSearches = MutableStateFlow<List<String>>(
        listOf(
            "今日热点", "科技新闻", "娱乐八卦", "体育赛事",
            "美食推荐", "旅游攻略", "电影推荐", "音乐榜单"
        )
    )
    val hotSearches: StateFlow<List<String>> = _hotSearches.asStateFlow()

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        // 从本地加载搜索历史
        _searchHistory.value = listOf("Android开发", "Jetpack Compose", "Kotlin")
    }

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // 添加到搜索历史
            addToHistory(query)

            // 这里应该调用搜索API
            // 目前使用公共时间线作为示例
            _searchResults.value = emptyList()

            _isLoading.value = false
        }
    }

    private fun addToHistory(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query) // 移除重复的
        currentHistory.add(0, query) // 添加到开头
        _searchHistory.value = currentHistory.take(10) // 保留最近10条
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }
}