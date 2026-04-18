package com.chweibo.android.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.repository.DraftRepository
import com.chweibo.android.data.repository.WeiboRepository
import com.chweibo.android.utils.RateLimitManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val weiboRepository: WeiboRepository,
    private val draftRepository: DraftRepository,
    private val rateLimitManager: RateLimitManager
) : ViewModel() {

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    private val _postSuccess = MutableSharedFlow<Boolean>()
    val postSuccess: SharedFlow<Boolean> = _postSuccess.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _canPost = MutableStateFlow(false)
    val canPost: StateFlow<Boolean> = combine(
        content,
        isPosting
    ) { content, isPosting ->
        content.isNotBlank() && !isPosting
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun addImages(uris: List<Uri>) {
        _selectedImages.value = _selectedImages.value + uris
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }

    fun postWeibo() {
        viewModelScope.launch {
            if (_content.value.isBlank()) {
                _errorMessage.emit("请输入内容")
                return@launch
            }

            // 检查速率限制
            val (canCall, waitTime) = rateLimitManager.canMakeCall("post")
            if (!canCall) {
                _errorMessage.emit(rateLimitManager.getWaitMessage("post"))
                return@launch
            }

            _isPosting.value = true

            try {
                rateLimitManager.recordCall("post")
                val result = if (_selectedImages.value.isEmpty()) {
                    // 纯文本微博
                    weiboRepository.postWeibo(_content.value)
                } else {
                    // 带图片的微博（这里简化处理，只上传第一张图片）
                    val imageFile = File(_selectedImages.value.first().path ?: "")
                    weiboRepository.postWeiboWithImage(_content.value, imageFile)
                }

                result.onSuccess {
                    _postSuccess.emit(true)
                    clear()
                }.onFailure { e ->
                    handleApiError(e, "发布失败")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "发布失败")
            } finally {
                _isPosting.value = false
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
        _errorMessage.emit(msg)
    }

    fun saveToDraft() {
        viewModelScope.launch {
            if (_content.value.isBlank() && _selectedImages.value.isEmpty()) {
                return@launch
            }

            draftRepository.createWeiboDraft(
                content = _content.value,
                imageUris = _selectedImages.value.map { it.toString() }
            )
            clear()
        }
    }

    private fun clear() {
        _content.value = ""
        _selectedImages.value = emptyList()
    }
}
