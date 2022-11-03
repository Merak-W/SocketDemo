package com.wyf.socket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wyf.socket.databinding.MessageLeftItemBinding
import com.wyf.socket.databinding.MessageRightItemBinding
import com.wyf.socket.databinding.MessageRvItemBinding

class MsgAdapter (private val msgList: ArrayList<Message>): RecyclerView.Adapter<MsgAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: MessageRvItemBinding):
        RecyclerView.ViewHolder(itemView.root) {
        var mView: MessageRvItemBinding
        init {
            mView = itemView
        }
    }

    //用于加载RecyclerView子项的布局
    //返回一个ViewHolder对象
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(MessageRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    //为子项绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = msgList[position]
        if(message.type == Message.TYPE_RECEIVED) {
            holder.mView.tvLeftMsg.text = message.content
        } else {
            holder.mView.tvRightMsg.text = message.content
        }
        holder.mView.leftMsg.visibility =
            if (message.type == Message.TYPE_RECEIVED) View.VISIBLE else View.INVISIBLE
        holder.mView.rightMsg.visibility =
            if (message.type == Message.TYPE_SENT) View.VISIBLE else View.INVISIBLE
    }

    //获取RecyclerView子项数量
    override fun getItemCount() = msgList.size

}