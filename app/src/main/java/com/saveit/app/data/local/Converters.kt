package com.saveit.app.data.local

import androidx.room.TypeConverter
import com.saveit.app.data.model.DownloadStatus
import com.saveit.app.data.model.MediaType
import com.saveit.app.data.model.Platform

class Converters {
    @TypeConverter
    fun fromPlatform(value: Platform): String = value.name

    @TypeConverter
    fun toPlatform(value: String): Platform = Platform.valueOf(value)

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}
