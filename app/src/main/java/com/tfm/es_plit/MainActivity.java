package com.tfm.es_plit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.activities.UserAccountActivity;

public class MainActivity extends AppCompatActivity {

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnLogin);

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserAccountActivity.class);
            startActivity(intent);
        });
    }
}