package com.swiftward.di

import android.content.Context
import com.swiftward.data.api.SwiftWardApi
import com.swiftward.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideSwiftWardApi(): SwiftWardApi {
        return Retrofit.Builder()
            // FIXED: Changed placeholder URL to your Node.js server address
            .baseUrl("http://10.0.2.2:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwiftWardApi::class.java)
    }
}