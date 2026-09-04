package com.liyaan.sound

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.liyaan.sound.databinding.ActivitySoundListBinding
import com.liyaan.sound.databinding.ActivitySplashBinding

class SoundListActivity:AppCompatActivity() {
    private val bindView:ActivitySoundListBinding by lazy {
        ActivitySoundListBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindView.root)
        bindView.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        bindView.recyclerView.adapter = SoundListAdapter(this)
    }
}