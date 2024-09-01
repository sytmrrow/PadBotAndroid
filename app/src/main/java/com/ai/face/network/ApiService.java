package com.ai.face.network;

import com.ai.face.bean.MeetingData;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
public interface ApiService {

    //@POST("api/testApp")
    @POST("conference_room/ordinary/reserveRoomRobotWithFeedBack")
    Call<ResponseBody> sendData(@Body MeetingData data);

    @GET("conference_room/ordinary/getAllReservationByRobot")
    Call<ResponseBody> getReservations();

    @POST("")
    Call<ResponseBody> login();

    @POST("app/api/processRequest_origin")
    Call<ResponseBody> sendData(@Body PatrobotData patrobotData);
}
