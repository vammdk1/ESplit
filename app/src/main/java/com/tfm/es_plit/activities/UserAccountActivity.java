package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;

public class UserAccountActivity extends AppCompatActivity {

    Button btnPaymentRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        btnPaymentRoom = findViewById(R.id.btnPaymentRoom);

        btnPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PaymentRoomActivity.class);
            startActivity(intent);
        });
    }
}