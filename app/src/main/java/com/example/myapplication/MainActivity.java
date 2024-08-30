package com.example.myapplication;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.Activity.ClientActivity;


import com.example.Activity.WebViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 确保布局文件存在

        // 找到按钮并设置点击事件
        Button videoButton = findViewById(R.id.videoButton);
        Button webButton = findViewById(R.id.webButton);
        Button meetingButton = findViewById(R.id.meetingButton);
        Button clientButton = findViewById(R.id.clientButton);
        Button buttonOpenTtsDemo = findViewById(R.id.button_open_tts_demo);

        buttonOpenTtsDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建 Intent 跳转到 TTSChinese 模块的 DemoActivity
                Intent intent = new Intent(MainActivity.this, com.air4.ttschineseDemo.DemoActivity.class);
                startActivity(intent);
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 VideoActivity
                Intent intent = new Intent(MainActivity.this, com.example.Activity.VideoActivity.class);
                startActivity(intent);
            }
        });

        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 WebActivity
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });

        meetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 MeetingActivity
                Intent intent = new Intent(MainActivity.this, com.example.Activity.MeetingActivity.class);
                startActivity(intent);
            }
        });

        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 ClientActivity
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });
    }
}