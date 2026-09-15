package com.saveit.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.saveit.app.data.model.DownloadItem

@Database(
    entities = [DownloadItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
