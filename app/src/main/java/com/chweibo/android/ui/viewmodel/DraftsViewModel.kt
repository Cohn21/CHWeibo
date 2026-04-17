package com.chweibo.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chweibo.android.data.model.Draft
import com.chweibo.android.data.repository.DraftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(
    private val draftRepository: DraftRepository
) : ViewModel() {

    val drafts: StateFlow<List<Draft>> = draftRepository.getAllDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDraft(draft: Draft) {
        viewModelScope.launch {
            draftRepository.deleteDraft(draft)
        }
    }

    fun clearAllDrafts() {
        viewModelScope.launch {
            draftRepository.clearAllDrafts()
        }
    }
}
