package com.iknowmuch.devicemanager.mqtt

enum class MQTTStatus {
    /**
     * Client is Connecting
     */
    CONNECTING,

    /**
     * Client is Connected
     */
    CONNECTED,

    /**
     * Client is Disconnecting
     */
    DISCONNECTING,

    /**
     * Client is Disconnected
     */
    DISCONNECTED,

    /**
     * 客户端连接成功
     */
    CONNECT_SUCCESS,

    /**
     * 客户端连接失败
     */
    CONNECT_FAIL,

    /**
     * 客户端连接丢失
     */
    CONNECT_LOST,

    /**
     * 客户端连接错误
     */
    CONNECT_ERROR,

    /**
     * 客户端连接状态未知
     */
    CONNECT_NONE,

    /**
     * 主题订阅成功
     */
    SUBSCRIBE_SUCCESS,

    /**
     * 主题订阅失败
     */
    SUBSCRIBE_FAIL,

    /**
     * 主题取消订阅成功
     */
    UNSUBSCRIBE_SUCCESS,

    /**
     * 主题取消订阅失败
     */
    UNSUBSCRIBE_FAIL,

    /**
     * 消息发布成功
     */
    PUBLISH_SUCCESS,

    /**
     * 消息发布失败
     */
    PUBLISH_FAIL
}