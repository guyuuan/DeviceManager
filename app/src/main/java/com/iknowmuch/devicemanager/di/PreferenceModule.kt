package com.iknowmuch.devicemanager.di

import com.iknowmuch.devicemanager.preference.AutoJumpTimePreference
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import com.iknowmuch.devicemanager.preference.HttpServerPreference
import com.iknowmuch.devicemanager.preference.KeepLivePreference
import com.iknowmuch.devicemanager.preference.MqttServerPreference
import com.tencent.mmkv.MMKV
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *@author: Chen
 *@createTime: 2021/8/13 16:48
 *@description:
 **/
@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {
    @Provides
    @Singleton
    fun provideMMKV() = MMKV.defaultMMKV() ?: throw RuntimeException("Can't get default mmkv")

    @Provides
    @Singleton
    fun provideDeviceIDPreference(mmkv: MMKV) = DeviceIDPreference(mmkv)

    @Provides
    @Singleton
    fun provideHttpServerPreference(mmkv: MMKV) = HttpServerPreference(mmkv)

    @Provides
    @Singleton
    fun provideMqttServerPreference(mmkv: MMKV) = MqttServerPreference(mmkv)

    @Provides
    @Singleton
    fun provideKeepLivePreference(mmkv: MMKV) = KeepLivePreference(mmkv)

    @Provides
    @Singleton
    fun provideAutoJumpTimePreference(mmkv: MMKV) = AutoJumpTimePreference(mmkv)
}