package com.wyf.socket.client

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SocketClient {
    private val TAG = SocketClient::class.java.simpleName

    private const val SOCKET_PORT = 9527

    private var socket: Socket? = null

    private var outputStream: OutputStream? = null
    private var inputStreamReader: InputStreamReader? = null

    private lateinit var mCallback: ClientCallback

    //声明线程池
    private var clientThreadPoll: ExecutorService? = null

    //连接服务
    fun connectServer(ipAddress: String, callback: ClientCallback) {
        mCallback = callback
        Thread {
            try {
                socket = Socket(ipAddress, SOCKET_PORT)
                ClientThread(socket!!, mCallback).start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    //关闭连接
    fun closeConnect() {
        inputStreamReader?.close()
        outputStream?.close()
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        clientThreadPoll?.shutdownNow()
        clientThreadPoll = null
        Log.d(TAG, "关闭连接")
    }

    //发送数据至服务器
    fun sendToServer(msg: String) {
        if(clientThreadPoll == null) {
            clientThreadPoll = Executors.newCachedThreadPool()
        }
        clientThreadPoll?.execute {
            if(socket == null) {
                mCallback.otherMsg("客户端未连接")
                return@execute
            }
            if(socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(msg.toByteArray())
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向服务端发送消息：$msg 失败")
            }
        }
    }

    class ClientThread(private val socket: Socket, private val callback: ClientCallback) :
        Thread() {
        override fun run() {
            val inputStream: InputStream?
            try {
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if (inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                    if (len < 1024) {
                        callback.receiveSeverMsg(receiveStr)
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("socket error", it) }
                callback.receiveSeverMsg("")
            }
        }
    }
}