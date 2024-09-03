package com.example.Activity;

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ai.face.bean.PatrobotData;
import com.ai.face.network.ApiService;
import com.ai.face.network.RetrofitClient;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.example.myapplication.R;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

public class VideoActivity extends AppCompatActivity implements RecognitionListener {

    private SimpleExoPlayer player;
    private Button playPauseButton;
    private boolean isPlaying = true;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private TextView recordWordView;
    private static OkHttpClient client;
    private boolean shouldSendRequest = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private VoskActivity voskActivity;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private int previousVolume; // 用于保存之前的音量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        client = new OkHttpClient();

        PlayerView playerView = findViewById(R.id.playerView);
        playPauseButton = findViewById(R.id.playPauseButton);
        Button backButton = findViewById(R.id.backButton);
        Button testButton = findViewById(R.id.test_button);

        setupAudioFocus();
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        testButton.setOnClickListener(v -> {
            PatrobotData data = new PatrobotData();
            data.setPrompt("你好");
            sendRequestToServer(data);
        });

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8082/video/Sample.mp4")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String videoUrl = "http://10.0.2.2:8082/video/Sample.mp4";
                    MediaItem mediaItem = MediaItem.fromUri(videoUrl);
                    runOnUiThread(() -> {
                        player.setMediaItem(mediaItem);
                        player.prepare();
                        player.play();
                    });
                }
            }
        });

        backButton.setOnClickListener(v -> finish());
        playPauseButton.setOnClickListener(v -> togglePlayback());
        recordWordView = findViewById(R.id.recordword);

        LibVosk.setLogLevel(LogLevel.INFO);
        checkPermissionAndInitialize();
    }

    private void checkPermissionAndInitialize() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            initModel();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    private void initModel() {
        StorageService.unpack(this, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    recognizeMicrophone();
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initModel();
        } else {
            finish();
        }
    }

    private void setupAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (speechService != null) {
                    speechService.stop();
                    speechService = null;
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                recognizeMicrophone();
            }
        };
    }

    private void recognizeMicrophone() {
        int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (speechService != null) {
                speechService.stop();
                speechService = null;
            }
            try {
                // 保存当前音量
                previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // 获取最大音量值
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                // 计算 10% 的音量
                int targetVolume = (int) (maxVolume * 0.1);
                // 降低音量到 10%
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);

                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState("Error starting speech service: " + e.getMessage());
            }
        }
    }

    private void togglePlayback() {
        if (isPlaying) {
            player.pause();
            playPauseButton.setText("播放");
        } else {
            player.play();
            playPauseButton.setText("暂停");
        }
        isPlaying = !isPlaying;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
            // 恢复音量
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
        }
        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }

    @Override
    public void onResult(String hypothesis) {
        String text = extractTextFromJson(hypothesis, "text");
        recordWordView.setText(text + "\n");
        if (text != null && text.contains("你好")) {
            shouldSendRequest = true;
            pauseVideo();
            return;
        }
        if (shouldSendRequest) {
            shouldSendRequest = false;
            if (text != null && !text.isEmpty()) {
                PatrobotData patrobotData = new PatrobotData();
                patrobotData.setPrompt(text);
                sendRequestToServer(patrobotData);
            }
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
    }

    public void pauseVideo() {
        if (player != null && player.isPlaying()) {
            player.pause();
            runOnUiThread(() -> {
                playPauseButton.setText("播放");
                isPlaying = false;
            });
        }
    }

    private void sendRequestToServer(PatrobotData patrobotData) {
        Gson gson = new Gson();
        String json = gson.toJson(patrobotData);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8082/api/test")
                .post(body)
                .build();

        Log.d("Request JSON", json);
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("API Failure", "Error: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("API Response", responseData);
                    mainHandler.post(() -> {
                        ResponseHandler responseHandler = new ResponseHandler(VideoActivity.this);
                        responseHandler.handleResponse(responseData);
                    });
                } else {
                    Log.e("API Error", "Error code: " + response.code());
                }
            }
        });
    }

    @Override
    public void onPartialResult(String hypothesis) {
    }

    private String extractTextFromJson(String json, String fieldName) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(fieldName);
        } catch (JSONException e) {
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
}
