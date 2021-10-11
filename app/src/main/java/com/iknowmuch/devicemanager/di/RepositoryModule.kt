package com.iknowmuch.devicemanager.di

import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.CabinetApiRepository
import com.iknowmuch.devicemanager.repository.DoorApiRepository
import com.iknowmuch.devicemanager.repository.DoorDataBaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}