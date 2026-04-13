package com.borderrun.app.di

import com.borderrun.app.data.remote.api.CountryApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/** Base URL for the RestCountries API. Version path is included in each endpoint. */
private const val BASE_URL = "https://restcountries.com/"

/**
 * Hilt module that provides the network stack: [OkHttpClient], [Retrofit],
 * and [CountryApiService].
 *
 * The logging interceptor uses [HttpLoggingInterceptor.Level.BODY] in debug
 * builds. In a production build, logging should be reduced to
 * [HttpLoggingInterceptor.Level.NONE].
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the singleton [OkHttpClient] with a logging interceptor.
     *
     * @return Configured [OkHttpClient] instance.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provides the singleton [Retrofit] instance targeting [BASE_URL].
     *
     * Uses [GsonConverterFactory] to deserialise API responses into
     * [com.borderrun.app.data.remote.dto.CountryDto] objects.
     *
     * @param okHttpClient The [OkHttpClient] to attach to Retrofit.
     * @return Configured [Retrofit] instance.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Provides the [CountryApiService] created by Retrofit.
     *
     * @param retrofit The singleton [Retrofit] instance.
     * @return Retrofit-generated implementation of [CountryApiService].
     */
    @Provides
    @Singleton
    fun provideCountryApiService(retrofit: Retrofit): CountryApiService =
        retrofit.create(CountryApiService::class.java)
}
