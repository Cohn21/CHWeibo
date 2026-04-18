package com.chweibo.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chweibo.android.data.local.dao.DraftDao
import com.chweibo.android.data.local.dao.WeiboDao
import com.chweibo.android.data.model.Draft
import com.chweibo.android.data.model.WeiboPostEntity

@Database(
    entities = [
        WeiboPostEntity::class,
        Draft::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeiboDatabase : RoomDatabase() {

    abstract fun weiboDao(): WeiboDao
    abstract fun draftDao(): DraftDao

    companion object {
        @Volatile
        private var INSTANCE: WeiboDatabase? = null

        fun getDatabase(context: Context): WeiboDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeiboDatabase::class.java,
                    "weibo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
