package com.tfm.es_plit.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "esplit_session";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1); // -1 = no hay sesión
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public void clearSession() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}