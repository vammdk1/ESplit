package com.tfm.es_plit.data.oldData;

import android.content.Context;
import com.google.gson.Gson;
import com.tfm.es_plit.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public User getUserById(int id) {
        List <User> users = getUsers();
        for (User user: users){
            if (user.getId() == id){
                return user;
            }
        }
        return null;
    }

    public User getUserByEmail(String email){
        List <User> users = getUsers();
        for (User user: users){
            if (Objects.equals(user.getEmail(), email)){
                return user;
            }
        }
        return null;
    }

    public boolean loggin (String email, String pass) {
        List<User> users = getUsers();
        for (User user : users) {
            if (Objects.equals(user.getEmail(), email)) {
                if(Objects.equals(user.getPassword(), pass)){
                    return true;
                }
            }
        }
        return false;
    }
}
