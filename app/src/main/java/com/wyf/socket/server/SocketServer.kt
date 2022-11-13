package com.wyf.socket.server

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SocketServer {
    private val TAG = SocketServer::class.java.simpleName

    private const val SOCKET_PORT = 9527

    private var socket: Socket? = null
    private var serverSocket: ServerSocket? = null

    private lateinit var mCallback: ServerCallback
    private lateinit var outputStream: OutputStream

    //声明线程池
    private var serverThreadPool: ExecutorService? = null

    var result = true

    //开启服务
    fun startServer(callback: ServerCallback): Boolean {
        mCallback = callback
        Thread {
            try {
                serverSocket = ServerSocket(SOCKET_PORT)
                while (result) {
                    socket = serverSocket?.accept()
                    mCallback.otherMsg("${socket?.inetAddress} to connected")
                    ServerThread(socket!!, mCallback).start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                result = false
            }
        }.start()
        return result
    }

    //关闭服务
    fun stopServer() {
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        serverSocket?.close()

        //关闭线程池
        serverThreadPool?.shutdownNow()
        serverThreadPool = null
        Log.d(TAG, "关闭连接")
    }

    //发送到客户端
    fun sendToClient(msg: String) {
        if(serverThreadPool == null) {
            serverThreadPool = Executors.newCachedThreadPool()  //没有核心线程，全是非核心线程，存活时间60s
        }
        serverThreadPool?.execute {
            //没有客户端连接时防止空指针异常造成闪退
            if(socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@execute
            }
            if(socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket!!.getOutputStream()
            try {
                outputStream.write(msg.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向客户端发送消息：$msg 失败")
            }
        }
    }

    class  ServerThread(private val socket: Socket, private val callback: ServerCallback) :
        Thread() {
        override fun run() {
            val inputStream: InputStream?
            try {
                //得到输入流，获取字节数据后转成String
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if(inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                    if(len < 1024) {
                        callback.receiveClientMsg(true, receiveStr)
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("socket error", it) }
                callback.receiveClientMsg(false, "")
            }
        }
    }
}