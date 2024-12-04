package com.example.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Handler handler = new Handler();
    private Runnable closeRunnable;
    private static final long TIMEOUT = 30000; // 30秒无操作则关闭

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // 初始化 WebView
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new CustomWebViewClient()); // 自定义WebViewClient处理错误
        webView.getSettings().setJavaScriptEnabled(true); // 启用JavaScript
        webView.getSettings().setDomStorageEnabled(true);  // 启用DOM存储

        // 获取输入框和确定按钮
        EditText urlInput = findViewById(R.id.url_input);
        Button openButton = findViewById(R.id.open_button);

        openButton.setOnClickListener(v -> {
            String url = urlInput.getText().toString();
            if (!url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url; // 为 URL 添加 http 前缀
                }
                Intent intent = new Intent(this, FullScreenViewActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
                finish(); // 关闭当前的 Activity
            } else {
                Toast.makeText(this, "URL is empty", Toast.LENGTH_SHORT).show();
            }
        });


        // 初始化关闭Runnable
        closeRunnable = this::finish;

        // 启动计时器
        resetTimer();
    }

    // 自定义WebViewClient类处理错误
    private static class CustomWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(view.getContext(), "加载网页失败: " + description, Toast.LENGTH_SHORT).show();
        }
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
