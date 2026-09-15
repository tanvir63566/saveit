package com.saveit.app.di

import android.content.Context
import androidx.room.Room
import com.saveit.app.data.local.AppDatabase
import com.saveit.app.data.local.DownloadDao
import com.saveit.app.data.remote.CobaltApi
import com.saveit.app.data.remote.RedditApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRedditApi(okHttpClient: OkHttpClient): RedditApi {
        return Retrofit.Builder()
            .baseUrl("https://www.reddit.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCobaltApi(okHttpClient: OkHttpClient): CobaltApi {
        return Retrofit.Builder()
            .baseUrl("https://api.cobalt.tools/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CobaltApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "saveit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao {
        return database.downloadDao()
    }
}
