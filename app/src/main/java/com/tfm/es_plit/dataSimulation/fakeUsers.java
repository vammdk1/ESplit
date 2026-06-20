package com.tfm.es_plit.dataSimulation;

import android.content.Context;
import com.google.gson.Gson;
import com.tfm.es_plit.models.Participant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class fakeUsers {
    private final Context context;

    public fakeUsers(Context context){
        this.context = context;
    }

    public List<User> getUsers() {
        try {
            InputStream is = context.getAssets().open("users.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            User[] users = new Gson().fromJson(sb.toString(), User[].class);
            return Arrays.asList(users);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserName(int id) {
        List <User> users = getUsers();
        for (User user: users){
            if (user.getId() == id){
                return user.getName();
            }
        }
        return null;
    }
}
