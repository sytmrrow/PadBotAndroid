package com.example.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ai.face.search.FaceSearch1NActivity
import com.ai.face.search.LoginActivity
import com.example.myapplication.databinding.ActivityNaviBinding

class MeetingActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 来绑定布局
        viewBinding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // 在这里你可以添加你想要的任何初始化代码
        viewBinding.confReserve.setOnClickListener() {
            startActivity(Intent(this@MeetingActivity, LoginActivity::class.java))
        }
        viewBinding.faceSearch1N.setOnClickListener() {
            startActivity(Intent(this@MeetingActivity, FaceSearch1NActivity::class.java))
        }
    }
}

