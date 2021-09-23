package com.iknowmuch.devicemanager.di

import com.iknowmuch.devicemanager.db.CabinetDoorDataBase
import com.iknowmuch.devicemanager.repository.CabinetDoorRepository
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
    fun provideCabinetDoorRepository(cabinetDoorDataBase: CabinetDoorDataBase) =
        synchronized(CabinetDoorRepository::class) {
            CabinetDoorRepository(cabinetDoorDataBase.getCabinetDoorDao())
        }

}