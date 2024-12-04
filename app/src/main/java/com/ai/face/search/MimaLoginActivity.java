package com.ai.face.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.ai.face.network.ApiService;
import com.ai.face.network.RetrofitClient;
import com.ai.face.utils.VoicePlayer;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MimaLoginActivity extends AppCompatActivity {

    private EditText edstuname;
    private EditText edstunum;
    private Button mimalogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mima_login); // 确保布局文件正确

        edstuname = findViewById(R.id.edstuname);
        edstunum = findViewById(R.id.edstunum);
        mimalogin = findViewById(R.id.mimalogin);

        /*mimalogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edstuname.getText().toString();
                String studentNumber = edstunum.getText().toString();

                if (username.isEmpty() || studentNumber.isEmpty()) {
                    Toast.makeText(MimaLoginActivity.this, "请输入姓名和学号", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User(username, studentNumber);

                // 发送网络请求到后端
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                Call<ResponseBody> call = apiService.login();
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String jsonResponse = response.body().string();
                                JSONObject jsonObject = new JSONObject(jsonResponse);
                                boolean success = jsonObject.getBoolean("success");

                                if (success) {
                                    // 登录成功，跳转到新页面
                                    Intent intent = new Intent(MimaLoginActivity.this, ConfReserve.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 登录失败，显示错误信息
                                    String message = jsonObject.getString("message");
                                    Toast.makeText(MimaLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MimaLoginActivity.this, "服务器响应解析失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MimaLoginActivity.this, "服务器响应失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MimaLoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/


        mimalogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edstuname.getText().toString();
                String studentNumber = edstunum.getText().toString();

                if (username.isEmpty() || studentNumber.isEmpty()) {
                    Toast.makeText(MimaLoginActivity.this, "请输入姓名和学号", Toast.LENGTH_SHORT).show();
                    showCustomDialog("信息填写错误", "请输入姓名和学号！");
                    return;
                }

                User user = new User(username, studentNumber);

                Intent intent = new Intent();
                intent.setClass(MimaLoginActivity.this, ConfReserve.class);
                startActivity(intent);
            }

            private void showCustomDialog(String title, String message) {
                // 创建AlertDialog.Builder实例并设置样式
                AlertDialog.Builder builder = new AlertDialog.Builder(MimaLoginActivity.this, R.style.CustomDialogStyle);
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("取消", null);
                        /*.create()
                        .show();*/

                // 获取弹窗消息的资源ID，这里假设已经将消息文本转换为了资源ID
                int messageResId = getMessageResIdFromText();
                // 使用VoicePlayer播放消息
                VoicePlayer voicePlayer = VoicePlayer.getInstance();
                voicePlayer.init(MimaLoginActivity.this); // 确保已经初始化VoicePlayer
                voicePlayer.play(messageResId); // 播放消息

                // 创建并显示弹窗
                AlertDialog dialog = builder.create();
                dialog.show();

                // 获取消息视图并设置文本大小
                TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                }
            }

            // 这个方法需要你根据实际情况实现，将消息文本转换为对应的资源ID
            private int getMessageResIdFromText() {
                // 需要根据资源文件来具体实现这个方法
                // 返回对应的资源ID
                return R.raw.smile; // 替换为实际的资源ID
            }
        });
    }
}


class User {
    String username;
    String studentNumber;

    public User(String username, String studentNumber) {
        this.username = username;
        this.studentNumber = studentNumber;
    }
}
