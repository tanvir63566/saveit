package com.saveit.app.data.remote

import com.google.gson.JsonArray
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface RedditApi {
    @GET
    suspend fun getPostJson(
        @Url url: String,
        @Header("User-Agent") userAgent: String = "SaveIt/1.0 (Android)"
    ): Response<JsonArray>
}
