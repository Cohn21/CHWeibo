package com.chweibo.android.data.local.dao

import androidx.room.*
import com.chweibo.android.data.model.Draft
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun getAllDrafts(): Flow<List<Draft>>

    @Query("SELECT * FROM drafts WHERE id = :id")
    suspend fun getDraftById(id: Long): Draft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft): Long

    @Update
    suspend fun updateDraft(draft: Draft)

    @Delete
    suspend fun deleteDraft(draft: Draft)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Long)

    @Query("DELETE FROM drafts")
    suspend fun deleteAllDrafts()

    @Query("SELECT COUNT(*) FROM drafts")
    suspend fun getDraftCount(): Int
}
