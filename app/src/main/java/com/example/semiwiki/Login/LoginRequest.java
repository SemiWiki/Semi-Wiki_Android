package com.example.semiwiki.Login;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("accountId")
    private String accountId;

    @SerializedName("password")
    private String password;

    public LoginRequest(String accountId, String password) {
        this.accountId = accountId;
        this.password = password;
    }

    public String getAccountId() { return accountId; }
    public String getPassword() { return password; }
}
