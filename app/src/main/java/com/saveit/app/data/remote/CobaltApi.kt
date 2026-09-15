package com.saveit.app.data.remote

import com.saveit.app.data.remote.dto.CobaltRequest
import com.saveit.app.data.remote.dto.CobaltResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CobaltApi {
    @POST("/")
    suspend fun getDownloadUrl(
        @Body request: CobaltRequest,
        @Header("Accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<CobaltResponse>
}
