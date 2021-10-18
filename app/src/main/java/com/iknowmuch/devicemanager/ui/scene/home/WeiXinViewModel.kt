package com.iknowmuch.devicemanager.ui.scene.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iknowmuch.devicemanager.repository.WeiXinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 *@author: Chen
 *@createTime: 2021/10/14 10:00
 *@description:
 **/
@HiltViewModel
class WeiXinViewModel @Inject constructor(
    repository: WeiXinRepository
) : ViewModel() {
    val qrCode = repository.getQRCode()
        .stateIn(viewModelScope, SharingStarted.Lazily, WeiXinRepository.DownloadResult.Progress(0))
}