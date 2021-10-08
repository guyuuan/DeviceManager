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

    /*
    * 探头电量上报
    * {
    * "probeCode":"54245345",
    * "power":"100",
    * "deptId":"10"
    * }
*/
    @POST("/cgi-bin/reportPower")
    fun reportProbePower(@Body body: Map<String, Any>): DefaultResponseJson

    //充电异常上报
    @POST("/cgi-bin/abnormalCharging")
    fun abnormalCharging(
        @Query("cabinetCode") deviceID: String,
        @Query("probeCode") probeCode: String,
        @Query("createDate") createDate: String,
        @Query("deptId") deptId:String
    ): DefaultResponseJson

}