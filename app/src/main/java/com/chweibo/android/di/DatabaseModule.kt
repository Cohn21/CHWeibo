package com.chweibo.android.di

import android.content.Context
import androidx.room.Room
import com.chweibo.android.data.local.WeiboDatabase
import com.chweibo.android.data.local.dao.DraftDao
import com.chweibo.android.data.local.dao.WeiboDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeiboDatabase {
        return Room.databaseBuilder(
            context,
            WeiboDatabase::class.java,
            "weibo_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWeiboDao(database: WeiboDatabase): WeiboDao {
        return database.weiboDao()
    }

    @Provides
    fun provideDraftDao(database: WeiboDatabase): DraftDao {
        return database.draftDao()
    }
}
