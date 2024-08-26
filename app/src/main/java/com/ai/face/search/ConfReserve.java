package com.ai.face.search;

import static com.ai.face.network.RetrofitClient.retrofit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.ai.face.bean.MeetingData;
import com.ai.face.network.ApiService;
import com.ai.face.utils.VoicePlayer;
import com.ai.face.network.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.annotations.JsonAdapter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.jar.JarException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfReserve extends AppCompatActivity {

    private TextView matchedFaceTextView;
    private TextView etUsername;
    private Spinner spinnerDate, spinnerTimeSlot, spinnerMeetingRoom;
    private Button btnBook;
    private TableLayout tableLayout;
    private Handler handler = new Handler();

    // 保存预定状态的Map
    private Map<String, Boolean> bookingStatusMap;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_reserve);

        matchedFaceTextView = findViewById(R.id.matched_face_text_view);
        etUsername = findViewById(R.id.matched_face_text_view); // 确保 ID 正确
        spinnerDate = findViewById(R.id.spinner_date);
        spinnerTimeSlot = findViewById(R.id.spinner_time_slot);
        spinnerMeetingRoom = findViewById(R.id.spinner_meeting_room);
        btnBook = findViewById(R.id.btn_book);
        tableLayout = findViewById(R.id.table_layout);

        //设置定时调用查询功能更新表格数据
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAndUpdateTable(); // 调用新的方法来获取预定信息
                handler.postDelayed(this, 60000); // 每分钟更新一次
            }
        }, 60000);
        // 获取 Intent 中的 ID
        Intent intent = getIntent();
        if (intent != null) {
            String matchedFace = intent.getStringExtra("matchedFace"); // 获取匹配到的人脸信息
            if (matchedFace != null) {
                // 显示匹配到的人脸信息
                matchedFaceTextView.setText(matchedFace);
                // 将匹配到的 ID 填入 EditText 中
                etUsername.setText(matchedFace);
            }
        }

        // 初始化下拉框数据
        initializeSpinners();
        // 初始化预定状态Map
        initializeBookingStatus();
        // 初始化表格
        initializeTable();

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 每次日期选择变化时，调用 updateTable 方法
                updateTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerMeetingRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 每次日期选择变化时，调用 updateTable 方法
                updateTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerTimeSlot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 每次日期选择变化时，调用 updateTable 方法
                updateTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = spinnerDate.getSelectedItem().toString();
                String timeSlot = spinnerTimeSlot.getSelectedItem().toString();
                String meetingRoom = spinnerMeetingRoom.getSelectedItem().toString();
                String username = etUsername.getText().toString();

                if (username.isEmpty()) {
                    // 弹出自定义弹窗
                    showCustomDialog("预订信息有误", "请填写用户姓名");
                    return;
                } else {
                    MeetingData data = saveBookingData(date, timeSlot, meetingRoom, username);
                    //发送网络请求，进行会议室预定
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                    Call<ResponseBody> call = apiService.sendData(data);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    String jsonResponse = response.body().string();
                                    JSONObject jsonObject = new JSONObject(jsonResponse);
                                    String responseStr = jsonObject.getString("responseStr");
                                    //服务器响应结果处理逻辑
                                    if (isExpired(date, timeSlot)) {
                                        showCustomDialog("预订信息有误", "该时段已过期，不可预订");
                                    } else {
                                        if ("会议预定成功!".equals(responseStr)) {
                                            showCustomDialog("预定成功", "预订成功！");
                                            bookMeetingRoom(date, timeSlot, meetingRoom);
                                            updateTable();
                                        } else if ("和该会议室已有会议时间冲突，预定失败！".equals(responseStr)) {
                                            showCustomDialog("会议室已预定", "该会议室在此时段已被预定");
                                        } else if ("该会议室不存在".equals(responseStr)) {
                                            showCustomDialog("预订信息有误", "该会议室不存在");
                                        } else {
                                            showCustomDialog("会议预定失败", "网络不畅");
                                        }
                                    }
                                } catch (IOException e) {
                                    Log.e("Network Response", "Error reading network response: " + e.getMessage());
                                    // 在这里添加适当的处理代码，例如显示错误信息给用户
                                    showCustomDialog("网络错误", "无法读取服务器响应");
                                } catch (JSONException e) {
                                    Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage());
                                    showCustomDialog("JSON解析错误", "无法解析服务器响应");
                                }
                            } else {
                                // 处理请求失败
                                showCustomDialog("网络请求失败", "请检查网络连接");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            // 处理请求失败
                            Log.e("Network Error", "Request failed: " + t.getMessage());
                            t.printStackTrace();  // 打印堆栈跟踪以获得更多信息
                            showCustomDialog("网络请求失败", "请检查网络连接2");
                        }
                    });

                }
            }

            private void showCustomDialog(String title, String message) {
                // 创建AlertDialog.Builder实例并设置样式
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfReserve.this, R.style.CustomDialogStyle);
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
                voicePlayer.init(ConfReserve.this); // 确保已经初始化VoicePlayer
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

        // 定时更新表格以检查状态
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTable();
                handler.postDelayed(this, 60000); // 每分钟更新一次
            }
        }, 60000);
    }

    private void initializeSpinners() {
        // 初始化日期为当天、次日、后天
        String[] dates = new String[3];
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        dates[0] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[2] = sdf.format(calendar.getTime());

        ArrayAdapter<String> dateAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, dates) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置字体大小为20sp
                return view;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置展开时下拉列表项的字体大小
                return view;
            }
        };
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 设置DropDownViewResource
        spinnerDate.setAdapter(dateAdapter);

        // 动态生成24小时内每2小时间隔时间段
        String[] timeSlots = generateTimeSlots();
        ArrayAdapter<String> timeSlotAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置字体大小为20sp
                return view;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置展开时下拉列表项的字体大小
                return view;
            }
        };
        timeSlotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSlot.setAdapter(timeSlotAdapter);

        // 初始化会议室选择
        String[] meetingRooms = {"B205", "第一会议室327", "第三会议室329", "J123", "J226"};
        ArrayAdapter<String> roomAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, meetingRooms) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置字体大小为20sp
                return view;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32); // 设置展开时下拉列表项的字体大小
                return view;
            }
        };
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeetingRoom.setAdapter(roomAdapter);
    }

    private String[] generateTimeSlots() {
        // 从00:00开始每2小时一个时间段
        String[] timeSlots = new String[12];
        int hour = 0;
        for (int i = 0; i < 12; i++) {
            String start = String.format(Locale.getDefault(), "%02d:00", hour);
            hour += 2;
            String end = String.format(Locale.getDefault(), "%02d:00", hour);
            timeSlots[i] = start + "-" + end;
        }
        return timeSlots;
    }

    private void initializeBookingStatus() {
        bookingStatusMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        String[] dates = new String[3];
        dates[0] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[2] = sdf.format(calendar.getTime());

        String[] timeSlots = generateTimeSlots();
        String[] meetingRooms = {"B205", "第一会议室327", "第三会议室329", "J123", "J226"};

        for (String date : dates) {
            for (String timeSlot : timeSlots) {
                if (isExpired(date, timeSlot)) {
                    continue;  // 跳过已过期的时间段
                }
                for (String meetingRoom : meetingRooms) {
                    bookingStatusMap.put(date + "-" + timeSlot + "-" + meetingRoom, false);
                }
            }
        }
    }

    private void initializeTable() {
        tableLayout.removeAllViews();
        TableRow headerRow = new TableRow(this);
        tableLayout.addView(headerRow);

        updateTable();
    }

    private void updateTable() {
        // 移除表格中已有的数据行
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        String[] meetingRooms = {"B205", "第一会议室327", "第三会议室329", "J123", "J226"};
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 生成当天和次日、后日的日期
        String[] dates = new String[3];
        dates[0] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[1] = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dates[2] = sdf.format(calendar.getTime());

        String[] timeSlots = generateTimeSlots();

        // 获取用户选择的日期
        String selectedDate = spinnerDate.getSelectedItem().toString();

        // 按时间先后顺序排列时间段
        Arrays.sort(timeSlots, (t1, t2) -> {
            String startTime1 = t1.split("-")[0];
            String startTime2 = t2.split("-")[0];
            return startTime1.compareTo(startTime2);
        });

        for (String timeSlot : timeSlots) {
            if (isExpired(selectedDate, timeSlot)) {
                continue;  // 跳过已过期的时间段
            }
            TableRow row = new TableRow(this);
            row.addView(createTextView(selectedDate, false, 150));
            row.addView(createTextView(timeSlot, false, 200));

            for (String meetingRoom : meetingRooms) {
                String key = selectedDate + "-" + timeSlot + "-" + meetingRoom;
                @SuppressLint({"NewApi", "LocalSuppress"}) boolean isBooked = bookingStatusMap.getOrDefault(key, false);
                row.addView(createStatusTextView(selectedDate, timeSlot, meetingRoom, isBooked));
            }

            tableLayout.addView(row);
        }
    }

    private TextView createStatusTextView(String date, String timeSlot, String meetingRoom, boolean isBooked) {
        TextView textView = createTextView(isBooked ? "已预定" : "未预定", false, 200);
        long currentTime = System.currentTimeMillis();

        String[] timeRange = timeSlot.split("-");
        String endTimeString = date + " " + timeRange[1];
        long slotEndTime = convertTimeToMillis(endTimeString);
        String selectedDate = spinnerDate.getSelectedItem().toString();
        String selectedmeetingRoom = spinnerMeetingRoom.getSelectedItem().toString();
        String selectedTime = spinnerTimeSlot.getSelectedItem().toString();

        if (slotEndTime < currentTime) {
            textView.setText("已过期");
            textView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else if (isBooked) {
            textView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark)); // 标红
        } else {
            textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }

        if (date.equals(selectedDate) && timeSlot.equals(selectedTime) && meetingRoom.equals(selectedmeetingRoom)) {
            textView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }// 淡蓝色

        return textView;
    }

    private void bookMeetingRoom(String date, String timeSlot, String meetingRoom) {
        String key = date + "-" + timeSlot + "-" + meetingRoom;
        bookingStatusMap.put(key, true);
    }

    @SuppressLint("NewApi")
    private boolean isAlreadyBooked(String date, String timeSlot, String meetingRoom) {
        return bookingStatusMap.getOrDefault(date + "-" + timeSlot + "-" + meetingRoom, false);
    }

    private boolean isExpired(String date, String timeSlot) {
        String[] timeRange = timeSlot.split("-");
        String endTimeString = date + " " + timeRange[1];
        long slotEndTime = convertTimeToMillis(endTimeString);
        return slotEndTime < System.currentTimeMillis();
    }

    private long convertTimeToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            return sdf.parse(dateTime).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private TextView createTextView(String text, boolean isHeader, int width) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setLayoutParams(new TableRow.LayoutParams(width, TableRow.LayoutParams.MATCH_PARENT));
        textView.setSingleLine(false);
        textView.setEllipsize(null);
        textView.setMaxLines(1);
        //textView.setWidth(width); // 确保列宽度适应屏幕
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        if (isHeader) {
            textView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
        return textView;
    }

    //封装预定会议信息
    private MeetingData saveBookingData(String date, String timeSlot, String meetingRoom, String username) {
        MeetingData data = new MeetingData();
        data.name = meetingRoom;
        data.date = date;
        data.beginTime = timeSlot.split("-")[0] + ":00";
        data.endTime = timeSlot.split("-")[1] + ":00";
        data.mobile = "13000";
        data.mark = username;

        return data;
    }

    //获取已预定会议信息
    private void fetchAndUpdateTable() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getReservations(); // 调用 GET 方法
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        updateBookingStatus(jsonResponse); // 更新预定状态
                        runOnUiThread(() -> updateTable()); // 更新表格在主线程中执行
                    } catch (IOException e) {
                        Log.e("Network Response", "Error reading network response: " + e.getMessage());
                    }
                } else {
                    Log.e("Network Error", "Request failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Network Error", "Request failed: " + t.getMessage());
            }
        });
    }


    //解析数据，找到robot数据
    private void updateBookingStatus(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            bookingStatusMap.clear(); // 清空原有状态

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String user = jsonObject.getString("user");

                // 只处理 user 为 robot 的数据
                if ("robot".equals(user)) {
                    String date = jsonObject.getString("date");
                    String beginTime = jsonObject.getString("beginTime");
                    String endTime = jsonObject.getString("endTime");
                    String roomName = jsonObject.getString("name");

                    // 标记预定状态
                    markCellsAsBooked(date, beginTime, endTime, roomName);
                }
            }
        } catch (JSONException e) {
            Log.e("JSON Parsing", "Error parsing JSON: " + e.getMessage());
        }
    }


    //更新表格的方法
    private void markCellsAsBooked(String date, String beginTime, String endTime, String roomName) {
        String[] timeSlots = generateTimeSlots();
        for (String timeSlot : timeSlots) {
            String[] times = timeSlot.split("-");
            String start = times[0] + ":00"; // 添加秒
            String end = times[1] + ":00"; // 添加秒

            // 检查时间段是否重叠
            if (date.equals(date) && isTimeOverlap(start, end, beginTime, endTime)) {
                String key = date + "-" + timeSlot + "-" + roomName;
                bookingStatusMap.put(key, true); // 标记为已预定
            }
        }
    }


    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        return (start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0);
    }
}