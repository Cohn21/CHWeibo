package com.chweibo.android.data.local.dao

import androidx.room.*
import com.chweibo.android.data.model.WeiboPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeiboDao {

    @Query("SELECT * FROM weibo_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<WeiboPostEntity>>

    @Query("SELECT * FROM weibo_posts WHERE id = :id")
    suspend fun getPostById(id: Long): WeiboPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: WeiboPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<WeiboPostEntity>)

    @Delete
    suspend fun deletePost(post: WeiboPostEntity)

    @Query("DELETE FROM weibo_posts")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM weibo_posts WHERE cachedAt < :timestamp")
    suspend fun deleteOldPosts(timestamp: Long)
}
