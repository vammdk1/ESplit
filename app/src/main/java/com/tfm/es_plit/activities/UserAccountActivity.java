package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;
import com.tfm.es_plit.dataSimulation.User;
import com.tfm.es_plit.dataSimulation.fakeUsers;

public class UserAccountActivity extends AppCompatActivity {

    Button btnHostPaymentRoom;
    Button btnJoinPaymentRoom;
    TextView userNametext;
    TextView userFundsText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        btnHostPaymentRoom = findViewById(R.id.btnHost);
        userNametext = findViewById(R.id.userUsername);
        userFundsText = findViewById(R.id.userFunds);


        fakeUsers repository = new fakeUsers(this);
        User user = repository.getUserByEmail("admin@mail.com");

        userNametext.setText(user.getName());
        userFundsText.setText(String.format("%.2f €",user.getFunds()));

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