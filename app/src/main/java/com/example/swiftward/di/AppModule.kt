package com.swiftward.di

import android.content.Context
import com.example.swiftward.utils.LocationHelper
import com.swiftward.data.api.SwiftWardApi
import com.swiftward.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)

    @Provides @Singleton
    fun provideLocationHelper(@ApplicationContext context: Context): LocationHelper =
        LocationHelper(context)

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides @Singleton
    fun provideSwiftWardApi(client: OkHttpClient): SwiftWardApi =
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5001/")   // Android emulator → localhost
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwiftWardApi::class.java)
}