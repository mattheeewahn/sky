package com.skytrace.app.di

import android.content.Context
import androidx.room.Room
import com.skytrace.app.data.local.SkyTraceDatabase
import com.skytrace.app.data.local.dao.*
import com.skytrace.app.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SkyTraceDatabase {
        return Room.databaseBuilder(
            context,
            SkyTraceDatabase::class.java,
            "skytrace.db"
        ).fallbackToDestructiveMigration().build()
    }

    // DAOs
    @Provides fun provideObservationDao(db: SkyTraceDatabase): ObservationDao = db.observationDao()
    @Provides fun provideCollectionDao(db: SkyTraceDatabase): CollectionDao = db.collectionDao()
    @Provides fun provideAsteroidCandidateDao(db: SkyTraceDatabase): AsteroidCandidateDao = db.asteroidCandidateDao()
    @Provides fun provideSatelliteTleDao(db: SkyTraceDatabase): SatelliteTleDao = db.satelliteTleDao()
    @Provides fun provideCatalogCacheDao(db: SkyTraceDatabase): CatalogCacheDao = db.catalogCacheDao()
    @Provides fun provideStarMessageDao(db: SkyTraceDatabase): StarMessageDao = db.starMessageDao()

    // API Services
    @Provides
    @Singleton
    fun provideCelesTrakApi(client: OkHttpClient): CelesTrakApi {
        return Retrofit.Builder()
            .baseUrl(CelesTrakApi.BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CelesTrakApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJplApi(client: OkHttpClient): JplApi {
        return Retrofit.Builder()
            .baseUrl(JplApi.BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JplApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMpcApi(client: OkHttpClient): MpcApi {
        return Retrofit.Builder()
            .baseUrl(MpcApi.BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpcApi::class.java)
    }
}
