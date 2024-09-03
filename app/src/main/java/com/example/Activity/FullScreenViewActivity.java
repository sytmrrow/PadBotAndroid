package com.example.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.air4.chinesetts.tts.TtsManager;
import com.example.myapplication.R;

public class FullScreenViewActivity extends AppCompatActivity {

    private WebView fullScreenWebView;
    private Handler handler = new Handler();
    private Runnable closeRunnable;
    private TtsManager ttsManager;
    private static final long TIMEOUT = 30000; // 30秒无操作则关闭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_webview);

        // 初始化TTS模块
        ttsManager = TtsManager.getInstance();
        ttsManager.init(this); // 确保在Activity中初始化TTS

        fullScreenWebView = findViewById(R.id.fullscreen_webview);
        fullScreenWebView.setWebViewClient(new WebViewClient());
        fullScreenWebView.getSettings().setJavaScriptEnabled(true);
        fullScreenWebView.getSettings().setDomStorageEnabled(true);

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            Log.d("FullScreenViewActivity", "Loading URL: " + url);
            fullScreenWebView.loadUrl(url);
        } else {
            Log.d("FullScreenViewActivity", "No URL passed!");
        }

        // 初始化关闭Runnable
        closeRunnable = this::finish;

        // 启动计时器
        resetTimer();
    }


    // 重置计时器
    private void resetTimer() {
        handler.removeCallbacks(closeRunnable);
        handler.postDelayed(closeRunnable, TIMEOUT); // 重新启动计时器
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetTimer(); // 用户操作时重置计时器
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(closeRunnable); // 防止内存泄漏
    }
}
