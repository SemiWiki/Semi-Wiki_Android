package com.example.semiwiki.Login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {


    private static final String BASE_URL = "http://3.36.236.238:8080/";

    private static Retrofit retrofit;

    private static volatile String currentAccessToken = null;

    public static void setAccessToken(String token) {
        currentAccessToken = token;
    }
    public static String getAccessToken() {
        return currentAccessToken;
    }

    private static OkHttpClient buildClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request req = chain.request();

                    String path = req.url().encodedPath();
                    boolean authNotNeeded = path.contains("/auth/signin") || path.contains("/logout");

                    if (!authNotNeeded && currentAccessToken != null && !currentAccessToken.isEmpty()) {
                        req = req.newBuilder()
                                .header("Authorization", "Bearer " + currentAccessToken)
                                .build();
                    }
                    return chain.proceed(req);
                })
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(buildClient())
                    .build();
        }
        return retrofit;
    }

    public static AuthService getAuthService() {
        return getRetrofitInstance().create(AuthService.class);
    }
}
