package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;

public class UserAccountActivity extends AppCompatActivity {

    Button btnHostPaymentRoom;
    Button btnJoinPaymentRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        btnHostPaymentRoom = findViewById(R.id.btnHost);

        btnHostPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PreHostRoomActivity.class);
            startActivity(intent);
        });

        btnJoinPaymentRoom = findViewById(R.id.btnJoin);

        btnJoinPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PrePaymentJoinRoomActivity.class);
            startActivity(intent);
        });
    }
}