package com.iknowmuch.devicemanager.http.api

import androidx.annotation.IntRange
import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *@author: Chen
 *@createTime: 2021/9/29 11:05
 *@description:
 * 借
 * 1.门 打不开
 * 2.门开了,东西没拿,也没关
 * 3.门开了,东西没拿,门关上了
 * 4.门开了,东西拿了,门没关
 * 5.成功
 * 还:
 * 1.门开了,东西没放入,门也没关
 * 2.门开了,东西放入,门没关
 * 3.门开了,东西没放入,门关了
 * 4.归还成功
 **/
interface DoorApi {
    companion object{
        const val StateClose = 0
        const val StateOpen = 1
    }
//    @POST("/android/openDoor")
//    suspend fun openDoor(
//        @Query("state") @IntRange(from = 0, to = 1) state: Int,
//        @Query("deptId") deptId: String,
//        @Query("cabinetCode") deviceID: String,
//        @Query("doorNo") doorNo: Int
//    ): DefaultResponseJson

    @POST("/android/openDoor")
    suspend fun reportDoorState(
        @Query("state") @IntRange(from = 0, to = 1) state: Int,
        @Query("deptId") deptId: String,
        @Query("cabinetCode") deviceID: String,
        @Query("doorNo") doorNo: Int,
        @Query("probeState") probeState: Boolean
    ): DefaultResponseJson
}