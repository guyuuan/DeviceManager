package com.iknowmuch.devicemanager.di

import com.iknowmuch.devicemanager.preference.AutoJumpTimePreference
import com.iknowmuch.devicemanager.preference.ChargingTimePreference
import com.iknowmuch.devicemanager.preference.DeptIDPreference
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import com.iknowmuch.devicemanager.preference.HttpServerPreference
import com.iknowmuch.devicemanager.preference.KeepLivePreference
import com.iknowmuch.devicemanager.preference.LastMessageTimePreference
import com.iknowmuch.devicemanager.preference.MqttServerPreference
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.preference.SerialPortPathPreference
import com.iknowmuch.devicemanager.preference.TokenPreference
import com.iknowmuch.devicemanager.preference.UpdateRecordPreference
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

    @Provides
    @Singleton
    fun provideChargingTimePreference(mmkv: MMKV) = ChargingTimePreference(mmkv)

    @Provides
    @Singleton
    fun provideLastMessageTimePreference(mmkv: MMKV) = LastMessageTimePreference(mmkv)

    @Provides
    @Singleton
    fun provideDeptIDPreference(mmkv: MMKV) = DeptIDPreference(mmkv)

    @Provides
    @Singleton
    fun provideSerialPortPathPreference(mmkv: MMKV) = SerialPortPathPreference(mmkv)

    @Provides
    @Singleton
    fun provideTokenPreference(mmkv: MMKV) = TokenPreference(mmkv)

    @Provides
    @Singleton
    fun provideUpdateRecordPreference(mmkv: MMKV) = UpdateRecordPreference(mmkv)

    @Provides
    @Singleton
    fun providePreferenceManager(
        deviceIDPreference: DeviceIDPreference,
        deptIDPreference: DeptIDPreference,
        httpServerPreference: HttpServerPreference,
        mqttServerPreference: MqttServerPreference,
        keepLivePreference: KeepLivePreference,
        autoJumpTimePreference: AutoJumpTimePreference,
        chargingTimePreference: ChargingTimePreference,
        lastMessageTimePreference: LastMessageTimePreference,
        serialPortPathPreference: SerialPortPathPreference,
        tokenPreference: TokenPreference,
        updateRecordPreference: UpdateRecordPreference
    ) = synchronized(PreferenceManager::class) {
        PreferenceManager(
            deviceIDPreference,
            deptIDPreference,
            httpServerPreference,
            mqttServerPreference,
            keepLivePreference,
            autoJumpTimePreference,
            chargingTimePreference,
            lastMessageTimePreference,
            serialPortPathPreference,
            tokenPreference,
            updateRecordPreference
        )
    }
}