package com.iknowmuch.devicemanager.bean

import com.squareup.moshi.JsonClass

/**
 *@author: Chen
 *@createTime: 2021/9/28 11:55
 *@description:
 **/
@JsonClass(generateAdapter = true)
data class DefaultResponseJson(
    override val status: Int,
    override val msg: String?,
    val data: Any? = null
) : BaseJson()