package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadTaskEntity::class], version = 4, exportSchema = false)
abstract class LinkFlowDatabase : RoomDatabase() {
    abstract fun linkFlowDao(): LinkFlowDao

    companion object {
        @Volatile
        private var instance: LinkFlowDatabase? = null

        fun getInstance(context: Context): LinkFlowDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LinkFlowDatabase::class.java,
                    "linkflow_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
        }
    }
}
