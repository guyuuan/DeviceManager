package com.iknowmuch.devicemanager.ui.scene.home

import androidx.lifecycle.ViewModel
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 *@author: Chen
 *@createTime: 2021/9/15 10:47
 *@description:
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceIDPreference: DeviceIDPreference
) : ViewModel() {
    val deviceID by deviceIDPreference
}