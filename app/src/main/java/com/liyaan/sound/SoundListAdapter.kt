package com.liyaan.sound

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

class SoundListAdapter(activity: AppCompatActivity): RecyclerView.Adapter<SoundListAdapter.MyHolder>() {
    private val _activity = activity
    class MyHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById<AppCompatTextView>(R.id.item_tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.item_sound,null)
        return MyHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return Utils.dataList().size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.text = Utils.dataList().get(position).name
        holder.tvName.setOnClickListener {
            val intent = Intent(_activity,MainActivity::class.java)
            intent.putExtra("path",Utils.dataList().get(position).soundUrl)
            _activity.startActivity(intent);
        }
    }
}