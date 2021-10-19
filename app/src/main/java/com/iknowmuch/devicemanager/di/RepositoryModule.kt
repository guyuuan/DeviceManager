package com.iknowmuch.devicemanager.di

import android.content.Context
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.http.api.WeiXinApi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.CabinetApiRepository
import com.iknowmuch.devicemanager.repository.DoorApiRepository
import com.iknowmuch.devicemanager.repository.DoorDataBaseRepository
import com.iknowmuch.devicemanager.repository.MainRepository
import com.iknowmuch.devicemanager.repository.SerialPortDataRepository
import com.iknowmuch.devicemanager.repository.WeiXinRepository
import com.iknowmuch.devicemanager.serialport.SerialPortManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *@author: Chen
 *@createTime: 2021/8/14 20:47
 *@description:
 **/
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDoorDataBaseRepository(cabinetDoorDao: CabinetDoorDao) =
        synchronized(DoorDataBaseRepository::class) {
            DoorDataBaseRepository(cabinetDoorDao)
        }

    @Provides
    @Singleton
    fun provideDoorApiRepository(doorApi: DoorApi, preferenceManager: PreferenceManager) =
        synchronized(DoorApiRepository::class) {
            DoorApiRepository(doorApi = doorApi, preferenceManager = preferenceManager)
        }

    @Provides
    @Singleton
    fun provideCabinetApiRepository(cabinetApi: CabinetApi, preferenceManager: PreferenceManager) =
        synchronized(CabinetApiRepository::class) {
            CabinetApiRepository(cabinetApi, preferenceManager)
        }

    @ExperimentalUnsignedTypes
    @Provides
    @Singleton
    fun provideMainRepository(
        apiRepository: CabinetApiRepository,
        serialPortDataRepository: SerialPortDataRepository,
        dataBaseRepository: DoorDataBaseRepository
    ) = synchronized(MainRepository::class) {
        MainRepository(
            dataBaseRepository = dataBaseRepository,
            serialPortDataRepository = serialPortDataRepository,
            apiRepository = apiRepository
        )
    }

    @ExperimentalUnsignedTypes
    @Provides
    @Singleton
    fun provideSerialPortDataRepository(
        serialPortManager: SerialPortManager,
        preferenceManager: PreferenceManager
    ) = synchronized(SerialPortDataRepository::class) {
        SerialPortDataRepository(serialPortManager, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideWeiXinRepository(
        weiXinApi: WeiXinApi,
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ) =
        synchronized(WeiXinRepository::class) {
            WeiXinRepository(weiXinApi, context, preferenceManager)
        }
}