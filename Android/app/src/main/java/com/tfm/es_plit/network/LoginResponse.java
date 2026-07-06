package com.tfm.es_plit.network;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String token;
    @SerializedName("user_id") // mapea el campo "user_id" del JSON a la variable userId
    private int userId;

    public boolean isSuccess() {
        return success;
    }
    public String getToken() {
        return token;
    }
    public int getUserId() {
        return userId;
    }
}
