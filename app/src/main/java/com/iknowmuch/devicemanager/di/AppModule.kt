package com.iknowmuch.devicemanager.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.iknowmuch.devicemanager.db.CabinetDoorDataBase
import com.iknowmuch.devicemanager.http.moshi.moshi
import com.iknowmuch.devicemanager.mqtt.MqttManager
import com.iknowmuch.devicemanager.preference.HttpServerPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.eclipse.paho.android.service.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 *@author: Chen
 *@createTime: 2021/8/14 14:20
 *@description:
 **/
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {

            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor {
                    Log.d("Retrofit", "log: $it")
                }.also { it.level = HttpLoggingInterceptor.Level.BODY })
            }
        }.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(httpServerPreference: HttpServerPreference): Retrofit =
        synchronized(Retrofit::class) {
            val httpServer by httpServerPreference
            Retrofit.Builder()
                .baseUrl(httpServer)
                .client(getOkHttpClient())
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        moshi
                    )
                )
                .build()
        }

    @Provides
    @Singleton
    fun provideMqttManager() = MqttManager()

    @Provides
    @Singleton
    fun provideCabinetDoorDataBase(@ApplicationContext cxt: Context): CabinetDoorDataBase =
        synchronized(CabinetDoorDataBase::class) {
            Room.databaseBuilder(cxt, CabinetDoorDataBase::class.java, "cabinet-door-db")
                .build()
        }

}
