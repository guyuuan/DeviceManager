package com.iknowmuch.devicemanager.http.api

import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *@author: Chen
 *@createTime: 2021/9/28 11:51
 *@description:
 **/
interface ProbeApi {

    //探头电量上报
    @POST("/cgi-bin/reportPower")
    fun reportProbePower(@Body body: Map<String, Any>): DefaultResponseJson

    //
    @POST("/cgi-bin/abnormalCharging")
    fun abnormalCharging(
        @Query("cabinetCode") deviceID: String,
        @Query("probeCode") probeCode: String,
        @Query("createDate") createDate: String
    ): DefaultResponseJson
}