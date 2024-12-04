package com.example.Activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.air4.chinesetts.tts.TtsManager;
import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Handler;

public class ResponseHandler {
    private Context context;
    private Handler handler = new Handler();

    public ResponseHandler(Context context){

        this.context=context;
        TtsManager.getInstance().init(context);
    }

    public void handleResponse(String responseData){
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            String type = jsonResponse.getString("type");
            switch (type){
                case "A":
                    //处理A类型事件
                    break;
                case "B":
                    //打开会议室预定界面
                    Intent intent = new Intent(context,MeetingActivity.class);
                    context.startActivity(intent);
                    break;
                case "C":
                    //处理C类事件（智能问答）
                    handleCTypeResponse(jsonResponse);
                    break;
                default:
                    Log.e("ResponseHandler","未知类型："+type);
                    break;
            }
        }catch (JSONException e){
            Log.e("ResponseHandler","JSON解析错误"+e.getMessage());
        }
    }
    private void handleCTypeResponse(JSONObject jsonResponse){
        try{
            JSONObject response = jsonResponse.getJSONObject("response");
            JSONArray events = response.getJSONArray("events");

            for(int i = 0; i < events.length();i++){
                JSONObject event = events.getJSONObject(i);
                String eventType = event.getString("eventType");
                if("webpage".equals(eventType)){
                    String url = event.getJSONObject("data").getString("path");
                    openWebpage(url);
                }else if ("speech".equals(eventType)){
                    String content = event.getJSONObject("data").getString("content");
                    // 延迟播放语音，确保网页已打开
                    handler.postDelayed(() -> speak(content), 1000); // 延迟1秒
                }
            }
        }catch (JSONException e){
            Log.e("ResponseHandler","处理C类事件错误："+e.getMessage());
        }
    }
    private void speak(String content){
        //调用tts模块朗读内容
        TtsManager.getInstance().speak(content,1.0F,true);
    }

    private void openWebpage(String url){
        Intent intent = new Intent(context,FullScreenViewActivity.class);
        intent.putExtra("url",url);
        context.startActivity(intent);
    }
}
