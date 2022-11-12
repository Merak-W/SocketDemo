package com.wyf.socket

import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.wyf.socket.client.ClientCallback
import com.wyf.socket.client.SocketClient
import com.wyf.socket.databinding.ActivityMainBinding
import com.wyf.socket.bean.server.ServerCallback
import com.wyf.socket.bean.server.SocketServer

class MainActivity : AppCompatActivity(), ServerCallback, ClientCallback {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding

    private val message = ArrayList<Message>()
    private lateinit var msgAdapter: MsgAdapter

    //当前是否为服务端
    private var isServer = true

    //Socket服务是否打开
    private var openSocket = false

    //Socket服务是否连接
    private var connectSocket = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.tvIpAddress.text = "IP地址：${getIp()}"

        //服务端和客户端切换
        binding.tabsGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isServer = when (tab) {
                    binding.tabsGroup.getTabAt(0) -> true
                    binding.tabsGroup.getTabAt(1) -> false
                    else -> true
                }
                binding.layoutServer.visibility = if (isServer) View.VISIBLE else View.GONE
                binding.layoutClient.visibility = if (isServer) View.GONE else View.VISIBLE
                binding.etMessage.hint = if (isServer) "发送给客户端" else "发送给服务端"
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        //开启服务/关闭服务 服务端处理
        binding.btnStartService.setOnClickListener {
            openSocket =
                if (openSocket) {
                    SocketServer.stopServer();false
                } else {
                    SocketServer.startServer(this);true
                }
            //显示日志
            Toast.makeText(this,
                if (openSocket) "开启服务" else "关闭服务",
                Toast.LENGTH_SHORT).show()
            //改变button文字
            binding.btnStartService.text = if (openSocket) "关闭服务" else "开启服务"
        }

        //连接服务/断开连接 客户端处理
        binding.btnConnectService.setOnClickListener {
            val ip = binding.etIpAddress.text.toString()
            if (ip.isEmpty()) {
                Toast.makeText(
                    this, "请输入IP地址哦",
                    Toast.LENGTH_SHORT
                ).show();return@setOnClickListener
            }
            connectSocket = if (connectSocket) {
                SocketClient.closeConnect();false
            } else {
                SocketClient.connectServer(ip, this);true
            }
            Toast.makeText(this,
                if (connectSocket) "连接服务" else "关闭连接",
                Toast.LENGTH_SHORT).show()
            binding.btnConnectService.text = if (connectSocket) "关闭连接" else "连接服务"
        }

        //发送消息给服务端/客户端
        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            if (msg.isEmpty()) {
                Toast.makeText(
                    this, "请输入要发送的信息",
                    Toast.LENGTH_SHORT
                ).show();return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) {
                openSocket
            } else if (connectSocket) {
                connectSocket
            } else {
                false
            }
            if (!isSend) {
                Toast.makeText(
                    this, "当前未开启服务或未连接服务",
                    Toast.LENGTH_SHORT
                ).show();return@setOnClickListener
            }
            if (isServer) {
                SocketServer.sendToClient(msg)
            } else {
                SocketClient.sendToServer(msg)
            }
            //发送后清空输入框
            binding.etMessage.setText("")
            updateList(if(isServer) 0 else 1, msg)
        }

        msgAdapter = MsgAdapter(message)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = msgAdapter
        }
    }

    //更新列表
    private fun updateList(type:Int, msg: String) {
        message.add(Message(msg, type))
        runOnUiThread {
            (if (message.size == 0) 0 else message.size - 1).apply {
                msgAdapter.notifyItemChanged(this)
                binding.rvMsg.smoothScrollToPosition(this)
            }
        }

    }

    private fun getIp() =
        inToIp((applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress)

    private fun inToIp(ip: Int) =
        "${(ip and 0xFF)}.${(ip shr 8 and 0xFF)}.${(ip shr 16 and 0xFF)}.${(ip shr 24 and 0xFF)}"

    override fun receiveClientMsg(success: Boolean, msg: String) {
        updateList(Message.TYPE_SENT,msg)
    }

    override fun receiveSeverMsg(msg: String) {
        updateList(Message.TYPE_RECEIVED,msg)
    }

    override fun otherMsg(msg: String) {
        Log.d(TAG, msg)
    }
}