package com.saveit.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CobaltRequest(
    @SerializedName("url")
    val url: String,
    @SerializedName("videoQuality")
    val videoQuality: String = "720",
    @SerializedName("audioFormat")
    val audioFormat: String = "mp3",
    @SerializedName("isAudioOnly")
    val isAudioOnly: Boolean = false,
    @SerializedName("filenameStyle")
    val filenameStyle: String = "basic"
)
