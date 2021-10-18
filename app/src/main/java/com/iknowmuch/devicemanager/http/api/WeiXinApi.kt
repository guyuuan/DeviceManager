package com.iknowmuch.devicemanager.http.api

import com.iknowmuch.devicemanager.bean.QRCodeJson
import com.iknowmuch.devicemanager.bean.WXAccessToken
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *@author: Chen
 *@createTime: 2021/10/12 17:16
 *@description:
 **/
interface WeiXinApi {

    @POST("https://api.weixin.qq.com/wxa/getwxacodeunlimit")
    suspend fun getWXACode(
        @Query("access_token") token: String,
        @Body data: QRCodeJson
    ): Response<ResponseBody>

    @GET("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxf568d657e36cc71b&secret=edf27db7ea436bbe7cdd361e0ae7dd22")
    suspend fun getWXAccessToken(): WXAccessToken
}