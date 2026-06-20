package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;

public class PrePaymentJoinRoomActivity extends AppCompatActivity {

    Button btnCancel;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prejoinroom);

        btnCancel = findViewById(R.id.btnHostCancel);
        btnStart = findViewById(R.id.btnNextActivity);

        double amount = Double.parseDouble("150");
        btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(PrePaymentJoinRoomActivity.this, PaymentJoinRoomActivity.class);
                intent.putExtra("TOTAL_AMOUNT", amount);
                startActivity(intent);
        });


        btnCancel.setOnClickListener(v -> {
            finish();
        });

    }
}
