package com.saveit.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CobaltResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("pickerType")
    val pickerType: String? = null,
    @SerializedName("picker")
    val picker: List<CobaltPickerItem>? = null,
    @SerializedName("text")
    val text: String? = null
)

data class CobaltPickerItem(
    @SerializedName("url")
    val url: String,
    @SerializedName("thumb")
    val thumb: String? = null,
    @SerializedName("type")
    val type: String? = null
)
