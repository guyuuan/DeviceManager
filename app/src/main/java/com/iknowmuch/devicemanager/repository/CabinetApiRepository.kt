package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import com.iknowmuch.devicemanager.bean.HomeDataJson
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.preference.PreferenceManager

/**
 *@author: Chen
 *@createTime: 2021/10/9 17:08
 *@description:
 **/
class CabinetApiRepository(
    private val cabinetApi: CabinetApi,
    private val preferenceManager: PreferenceManager
) {
    suspend fun getHomeData(): HomeDataJson? {
        return if (preferenceManager.deptID.isNotEmpty()) {
            cabinetApi.getHomeData(preferenceManager.deviceID, preferenceManager.deptID)
        }else{
            null
        }
    }
}