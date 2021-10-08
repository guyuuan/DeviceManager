package com.iknowmuch.devicemanager.http.api

import androidx.annotation.IntRange
import com.iknowmuch.devicemanager.bean.DefaultResponseJson
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *@author: Chen
 *@createTime: 2021/9/29 11:05
 *@description:
 **/
interface DoorApi {

    @POST("/android/openDoor")
    fun openDoor(
        @Query("state") @IntRange(from = 0, to = 1) state: Int,
        @Query("probeCode") probeCode: String,
        @Query("cabinetCode") deviceID: String,
        @Query("doorNo") doorNo: Int
    ): DefaultResponseJson

    @POST("/android/closeDoor")
    fun closeDoor(
        @Query("state") @IntRange(from = 0, to = 1) state: Int,
        @Query("probeCode") probeCode: String,
        @Query("cabinetCode") deviceID: String,
        @Query("doorNo") doorNo: Int
    ): DefaultResponseJson
}