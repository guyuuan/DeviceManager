package com.iknowmuch.devicemanager.bean
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


/**
 *@author: Chen
 *@createTime: 2021/10/12 11:57
 *@description:
 **/
@JsonClass(generateAdapter = true)
data class CabinetDataJson(
    @Json(name = "cabinetCode")
    val cabinetCode: String,
    @Json(name = "deptId")
    val deptId: Int,
    @Json(name = "probes")
    val probes: List<Probe>
) {
    @JsonClass(generateAdapter = true)
    data class Probe(
        @Json(name = "availableTime")
        val availableTime: Int?,
        @Json(name = "doorNo")
        val doorNO: Int,
        @Json(name = "power")
        val power: Int?,
        @Json(name = "probeCode")
        val probeCode: String?
    )
}