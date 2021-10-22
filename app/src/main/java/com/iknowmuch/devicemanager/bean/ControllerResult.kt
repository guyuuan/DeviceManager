package com.iknowmuch.devicemanager.bean

/**
 *@author: Chen
 *@createTime: 2021/10/22 15:26
 *@description:
 **/
data class ControllerResult(
    //开的哪个门
    val doorNo: Int = 0,
    val status: Int = 0,
    val openState: Boolean = false,
    val closeState: Boolean = true
)
