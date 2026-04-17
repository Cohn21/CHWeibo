package com.chweibo.android.data.repository

import com.chweibo.android.data.local.dao.DraftDao
import com.chweibo.android.data.model.Draft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepository @Inject constructor(
    private val draftDao: DraftDao
) {
    fun getAllDrafts(): Flow<List<Draft>> = draftDao.getAllDrafts()

    suspend fun getDraftById(id: Long): Draft? = draftDao.getDraftById(id)

    suspend fun saveDraft(draft: Draft): Long {
        val updatedDraft = draft.apply {
            updatedAt = System.currentTimeMillis()
        }
        return if (draft.id > 0) {
            draftDao.updateDraft(updatedDraft)
            draft.id
        } else {
            draftDao.insertDraft(updatedDraft)
        }
    }

    suspend fun deleteDraft(draft: Draft) {
        draftDao.deleteDraft(draft)
    }

    suspend fun deleteDraftById(id: Long) {
        draftDao.deleteDraftById(id)
    }

    suspend fun clearAllDrafts() {
        draftDao.deleteAllDrafts()
    }

    suspend fun getDraftCount(): Int {
        return draftDao.getDraftCount()
    }

    suspend fun createWeiboDraft(
        content: String,
        imageUris: List<String> = emptyList(),
        isRepost: Boolean = false,
        sourceWeiboId: Long? = null
    ): Long {
        val draft = Draft(
            content = content,
            isRepost = isRepost,
            sourceWeiboId = sourceWeiboId
        ).apply {
            setImageUriList(imageUris)
        }
        return draftDao.insertDraft(draft)
    }
}
