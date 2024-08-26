package com.example.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.example.myapplication.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    private SimpleExoPlayer player;
    private Button playPauseButton;
    private boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        PlayerView playerView = findViewById(R.id.playerView);
        playPauseButton = findViewById(R.id.playPauseButton);
        Button backButton = findViewById(R.id.backButton);

        // 初始化ExoPlayer
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // 获取视频流
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://222.200.184.74:8082/video/Sample.mp4")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理失败情况
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 获取视频流成功
                    String videoUrl = "http://222.200.184.74:8082/video/Sample.mp4";
                    MediaItem mediaItem = MediaItem.fromUri(videoUrl);

                    // 使用 runOnUiThread 方法确保在主线程上执行播放器相关操作
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            player.setMediaItem(mediaItem);
                            player.prepare();
                            player.play();
                        }
                    });
                } else {
                    // 处理失败情况
                }
            }
        });

        // 返回按钮的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一层
            }
        });

        // 播放/暂停按钮的点击事件
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    player.pause();
                    playPauseButton.setText("播放");
                } else {
                    player.play();
                    playPauseButton.setText("暂停");
                }
                isPlaying = !isPlaying;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause(); // 在活动暂停时暂停视频
        isPlaying = false; // 更新状态
        playPauseButton.setText("播放"); // 更新按钮文本
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release(); // 释放资源
    }
}
