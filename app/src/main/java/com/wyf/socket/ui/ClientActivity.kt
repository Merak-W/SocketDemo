package com.wyf.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.wyf.socket.Message
import com.wyf.socket.MsgAdapter
import com.wyf.socket.R
import com.wyf.socket.client.ClientCallback
import com.wyf.socket.client.SocketClient
import com.wyf.socket.databinding.ActivityClientBinding
import com.wyf.socket.databinding.DialogEditIpBinding

class ClientActivity : BaseActivity(), ClientCallback {

    private val TAG = BaseActivity::class.java.simpleName
    private lateinit var binding: ActivityClientBinding

    private var connectSocket = false

    private val messages = ArrayList<Message>()

    private lateinit var msgAdapter: MsgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.tvConnectService.setOnClickListener {
            if (connectSocket) {
                SocketClient.closeConnect()
                connectSocket = false
                showToast("关闭连接")
            } else {
                showEditDialog()
            }
            binding.tvConnectService.text = if (connectSocket) "关闭连接" else "连接服务"
        }

        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMessage.text.toString().trim()
            if (msg.isEmpty()) {
                showToast("请输入要发送的信息");return@setOnClickListener
            }

            val isSend = if (connectSocket) connectSocket else false
            if (!isSend) {
                showToast("当前未开启服务或未连接服务");return@setOnClickListener
            }
            SocketClient.sendToServer(msg)
            binding.etMessage.setText("")
            updateList(Message.TYPE_SENT, msg)
        }

        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ClientActivity)
            adapter = msgAdapter
        }
    }

    private fun showEditDialog() {
        val dialogBinding =
            DialogEditIpBinding.inflate(LayoutInflater.from(this@ClientActivity), null, false)
        AlertDialog.Builder(this@ClientActivity).apply {
            setIcon(R.drawable.ic_connect)
            setTitle("连接IP地址")
            setView(dialogBinding.root)
            setPositiveButton("确定") { dialog, _ ->
                val ip = dialogBinding.etIpAddress.text.toString()
                if (ip.isEmpty()) {
                    showToast("请输入IP地址");return@setPositiveButton
                }
                connectSocket = true
                SocketClient.connectServer(ip, this@ClientActivity)
                showToast("连接服务")
                binding.tvConnectService.text = "关闭连接"
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
        }.show()
    }

    override fun receiveSeverMsg(msg: String) = updateList(Message.TYPE_RECEIVED, msg)

    override fun otherMsg(msg: String) {
        Log.d(TAG, msg)
    }

    private fun updateList(type: Int, msg: String) {
        messages.add(Message(msg, type))
        runOnUiThread {
            (if (messages.size == 0) 0 else messages.size - 1).apply {
                msgAdapter.notifyItemChanged(this)
                binding.rvMsg.smoothScrollToPosition(this)
            }
        }
    }
}