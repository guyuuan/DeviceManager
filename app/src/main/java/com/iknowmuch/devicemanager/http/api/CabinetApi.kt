package com.iknowmuch.devicemanager.http.api

import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import com.iknowmuch.devicemanager.bean.HomeDataJson
import com.iknowmuch.devicemanager.bean.ReturnProbeResult
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

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
    suspend fun heartBeat(@Body map: Map<String, @JvmSuppressWildcards Any>): DefaultResponseJson

    /*
    * cabinetCode: 设备编号
    * deptId: 园区id
    * */
    @POST("/homeDetails/getDetailsData")
    suspend fun getHomeData(
        @Query("cabinetCode") cabinetCode: String,
        @Query("deptId") deptId: String
    ): HomeDataJson

    /*
    * {
    *     "type":"1",    //0探头报警，1智能柜报警
    *     "cabinetCode":"delsmart210929174303",    //智能柜编码
    *     "probeCode":"",    //探头编码
    *     "state":"0",  //报警类型：探头：1借出超时未还，4探头故障，5探头遗失，6异常，7归还异常；
    *                             智能柜：0柜门未关，1智能柜离线，2硬件设备损坏
    *     "cabinetDoorNo":1  //柜门id
    *     "content":"123456",    //报警原因描述
    *     "deptId":"248",    //院区id
    *     "createTime":"2021-10-08 04:54:40",    //报警时间
    *     "lastTime":"2021-10-08 04:54:40"    //最新通知时间
    * }
    * */
    @POST("/cgi-bin/addAlarmRecord")
    suspend fun reportAlarm(@Body data: Map<String, String>): DefaultResponseJson

    @POST("/android/power")
    suspend fun reportCabinetData(@Body data: CabinetDataJson): DefaultResponseJson

    @POST("/cgi-bin/abnormalCharging")
    suspend fun reportProbeAbnormalCharging(@QueryMap data: Map<String, String>): DefaultResponseJson

    @POST("borrowAndReturn/scanReturn")
    suspend fun returnProbe(
        @Query("probeCode", encoded = true) probeCode: String,
        @Query("cabinetCode") cabinetCode: String,
        @Query("deptId") deptId: String
    ): ReturnProbeResult

    /*
    * {
    *   "upGradeStatus":"1",升级状态：0失败，1成功
    *   "appVersion":"Vsion2.0",当前版本
    *   "upGradeTime":"2021-10-20 10:10:10",升级时间
    *   "code":"yzq",智能柜编码
    *   "deptId":"252"院区id
    *  }
    * */
    @POST("/cgi-bin/appVersion")
    suspend fun reportUpdateResult(@Body data: Map<String, String>): DefaultResponseJson
}