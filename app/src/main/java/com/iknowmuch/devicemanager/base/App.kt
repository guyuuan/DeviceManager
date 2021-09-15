package com.iknowmuch.devicemanager.base

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

/**
 *@author: Chen
 *@createTime: 2021/9/13 9:47
 *@description:
 **/
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}