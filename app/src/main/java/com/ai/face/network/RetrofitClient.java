package com.ai.face.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// 在 RetrofitClient 中添加日志拦截器
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {
    private static final String BASE_URL = "http://222.200.184.32:8088/";
    private static final String BASE_URL2 = "http://222.200.184.32:8288/";
    public static Retrofit retrofit;
    public static Retrofit robotRetrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = createRetrofitInstance(BASE_URL);}
            }
        }
        return retrofit;
    }

    public static Retrofit getRobotClient() {
        if (robotRetrofit == null) {
            synchronized (RetrofitClient.class) {
                if (robotRetrofit == null) {
                    robotRetrofit = createRetrofitInstance(BASE_URL2);}
            }
        }
        return robotRetrofit;
    }

    /*public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRobotCilent(){
        if (robotRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            robotRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL2)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return robotRetrofit;
    }*/

    //私有工厂方法创建实例，避免冲突
    private static Retrofit createRetrofitInstance(String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
