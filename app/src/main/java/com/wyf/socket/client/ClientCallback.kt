package com.wyf.socket.client

interface ClientCallback {
    //接收服务端的消息
    fun receiveSeverMsg(msg: String)
    //其他消息
    fun otherMsg(msg: String)
}