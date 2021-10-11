package com.iknowmuch.devicemanager.http.api

import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import com.iknowmuch.devicemanager.bean.HomeDataJson
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *@author: Chen
 *@createTime: 2021/9/29 14:00
 *@description:
 **/
interface CabinetApi {
    /*
    * "code":"gui324243242344",
    * "updateTime":"2021-09-2716:37:47",
    * "deptId":"252"
    * "appVersion":"1.0"
    * */
    @POST("/cgi-bin/onlineStatus")
    suspend fun heartBeat(@Body map: Map<String, Any>)

    /*
    *cabinetCode: 设备编号
    * deptId: 园区id
    * */
    @POST("/homeDetails/getDetailsData")
    suspend fun getHomeData(
        @Query("cabinetCode") cabinetCode: String,
        @Query("deptId") deptId: String
    ): HomeDataJson
}