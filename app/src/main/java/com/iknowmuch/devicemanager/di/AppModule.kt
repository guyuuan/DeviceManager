package com.iknowmuch.devicemanager.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.iknowmuch.devicemanager.BuildConfig
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.db.CabinetDoorDataBase
import com.iknowmuch.devicemanager.db.dao.DeviceDao
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.http.moshi.moshi
import com.iknowmuch.devicemanager.mqtt.MqttManager
import com.iknowmuch.devicemanager.preference.HttpServerPreference
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.DeviceRepository
import com.iknowmuch.devicemanager.serialport.SerialPortManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
                .baseUrl(if (httpServer.startsWith(Config.HTTP) || httpServer.startsWith(Config.HTTPS)) httpServer else Config.HTTP + httpServer)
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
            Room.databaseBuilder(cxt, CabinetDoorDataBase::class.java, "cabinet-door.db")
                .createFromAsset("v1.db")
                //初始化使用的db文件的版本号必须和@Database(
                //    entities = [CabinetDoor::class],
                //    version = 1,)
                // 中的version数值保持一致
                .build()
        }

    @Provides
    @Singleton
    fun provideCabinetDoorDao(cabinetDoorDataBase: CabinetDoorDataBase) =
        cabinetDoorDataBase.getCabinetDoorDao()

    @Provides
    @Singleton
    fun provideDeviceDao(cabinetDoorDataBase: CabinetDoorDataBase) =
        cabinetDoorDataBase.getDeviceDao()

    @Provides
    @Singleton
    fun provideDeviceRepository(deviceDao: DeviceDao, preferenceManager: PreferenceManager) =
        synchronized(DeviceRepository::class) {
            DeviceRepository(deviceDao, preferenceManager)
        }

    @Provides
    @Singleton
    fun provideDoorApi(retrofit: Retrofit): DoorApi = synchronized(DoorApi::class) {
        retrofit.create(DoorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCabinetApi(retrofit: Retrofit): CabinetApi = synchronized(CabinetApi::class) {
        retrofit.create(CabinetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSerialPortManager() = synchronized(SerialPortManager::class.java) {
        SerialPortManager()
    }
}
