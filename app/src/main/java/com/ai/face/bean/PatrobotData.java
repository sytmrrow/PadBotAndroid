package com.ai.face.bean;

public class PatrobotData {
    public String sessionId = "fa4f3b996724480eaa05ff8e0930b07b";
    public String newTopic = "false";
    public String deviceId = "PX0001";
    public String deviceModel = "X1";
    public String city = "广州";
    public String district = "黄浦区";
    public String lang = "zh_cn";
    public String prompt;

    public String getSessionId() {return sessionId;}
    public String getNewTopic() {return newTopic;}
    public String getDeviceId() {return deviceId;}
    public String getDeviceModel() {return deviceModel;}
    public String getCity() {return city;}
    public String getDistrict() {return district;}
    public String getLang() {return lang;}

    public String getPrompt() {return prompt;}
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}