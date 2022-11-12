package com.wyf.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.wyf.socket.Message
import com.wyf.socket.MsgAdapter
import com.wyf.socket.bean.server.ServerCallback
import com.wyf.socket.bean.server.SocketServer
import com.wyf.socket.databinding.ActivityServerBinding

class ServerActivity : BaseActivity(), ServerCallback {

    private val TAG = ServerActivity::class.java.simpleName
    private lateinit var binding: ActivityServerBinding

    private var openSocket = false

    private val messages = ArrayList<Message>()

    private lateinit var msgAdapter: MsgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.apply {
            subtitle = "IP：${getIp()}"
            setNavigationOnClickListener { onBackPressed() }
        }

        //开启服务/连接服务
        binding.tvStartService.setOnClickListener {
            openSocket = if (openSocket) {
                SocketServer.stopServer();false
            } else SocketServer.startServer(this)
            showToast(if(openSocket)"开启服务" else "关闭服务")
            binding.tvStartService.text = if(openSocket)"关闭服务" else "开启服务"
        }

        //发送消息
        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMessage.text.toString().trim()
            if(msg.isEmpty()) {
                showToast("请输入要发送的消息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) openSocket else false
            if (!isSend) {
                showToast("当前未开启服务或未连接服务");return@setOnClickListener
            }
            SocketServer.sendToClient(msg)
            binding.etMessage.setText("")
            updateList(Message.TYPE_SENT, msg)
        }
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ServerActivity)
            adapter = msgAdapter
        }
    }

    //ToolBar返回箭头
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun receiveClientMsg(success: Boolean, msg: String) = updateList(Message.TYPE_RECEIVED, msg)

    override fun otherMsg(msg: String) {
        Log.d(TAG, msg)
    }

    private fun updateList(type: Int, msg: String) {
        messages.add(Message(msg, type))
        runOnUiThread {
            (if(messages.size == 0) 0 else messages.size - 1).apply {
                msgAdapter.notifyItemChanged(this)
                binding.rvMsg.smoothScrollToPosition(this)
            }
        }
    }
}