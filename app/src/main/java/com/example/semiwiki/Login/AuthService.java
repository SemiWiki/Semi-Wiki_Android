package com.example.semiwiki.Login;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
public interface AuthService {

    @POST("auth/signin")
    Call<LoginResponse> signIn(@Body LoginRequest request);

    @GET("logout/{accountId}")
    Call<Void> logout(@Path("accountId") String accountId);
}
