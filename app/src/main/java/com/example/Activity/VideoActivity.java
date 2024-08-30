package com.example.Activity;

import android.content.Intent;
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
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;
import org.vosk.demo.VoskActivity;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VideoActivity extends AppCompatActivity implements
        RecognitionListener {

    private SimpleExoPlayer player;
    private Button playPauseButton;
    private boolean isPlaying = true;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private TextView recordWordView; // 用于显示语音识别结果的TextView
    private VoskActivity voskActivity; // 用于语音识别的Activity

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

        // 初始化语音识别的TextView
        recordWordView = findViewById(R.id.recordword);
//        setUiState(STATE_START);

        LibVosk.setLogLevel(LogLevel.INFO);

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            initModel();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    private void initModel() {
        StorageService.unpack(this, "model-en-us", "model",
                (model) -> {
                    this.model = model;
//                    setUiState(STATE_READY);
                    // 如果模型已经初始化，不需要再次初始化，可以直接开始监听
                    if (model != null) {
                        recognizeMicrophone();
                    }
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                initModel();
                recognizeMicrophone(); // 开始监听麦克风
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }

    @Override
    public void onResult(String hypothesis) {
        String text = extractTextFromJson(hypothesis, "text");
        recordWordView.setText(text + "\n");

        // 当字段中出现“会议室”的文字，跳转到预定会议室的页面
        if (text != null && text.contains("会议室")) {
            // 创建Intent来启动新的Activity
            Intent intent = new Intent(VideoActivity.this, MeetingActivity.class);
            // 启动新的Activity
            startActivity(intent);
        }
    }

    @Override
    // 暂时用不着最终结果的方法(指录音结束的最后一条结果)
    public void onFinalResult(String hypothesis) {
        String text = extractTextFromJson(hypothesis, "text");
//        resultView.append("最终：" + text + "\n");
//        setUiState(STATE_DONE);
        if (speechStreamService != null) {
            speechStreamService = null;
        }
    }

    @Override
    // 暂时用不着部分词的方法
    public void onPartialResult(String hypothesis) {
        String partial = extractTextFromJson(hypothesis, "partial");
//        resultView.append("部分词：" + partial + "\n");
    }

    // 提取Json格式的识别结果hypothesis中的特定字段
    private String extractTextFromJson(String json, String fieldName) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(fieldName);
        } catch (JSONException e) {
            // 如果JSON解析失败或字段不存在，返回原始字符串或错误信息
            return "JSON解析错误或字段不存在";
        }
    }

    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {

    }



    private void setErrorState(String message) {
        recordWordView.setText(message);
    }

    private void recognizeMicrophone() {
        if (speechService != null) {
//            setUiState(STATE_DONE);
            speechService.stop();
            speechService = null;
        } else {
//            setUiState(STATE_MIC);
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }
}
