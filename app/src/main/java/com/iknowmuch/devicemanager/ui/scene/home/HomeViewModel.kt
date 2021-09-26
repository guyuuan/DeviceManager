package com.iknowmuch.devicemanager.ui.scene.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import com.iknowmuch.devicemanager.repository.CabinetDoorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 *@author: Chen
 *@createTime: 2021/9/15 10:47
 *@description:
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceIDPreference: DeviceIDPreference,
    private val repository: CabinetDoorRepository
) : ViewModel() {
    val deviceID by deviceIDPreference
    val cabinetDoorList = repository.getCabinetDoorFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )
}