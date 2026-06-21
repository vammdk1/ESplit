package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.data.fakeUsers;
import com.tfm.es_plit.network.UserRepository;

public class UserAccountActivity extends AppCompatActivity {

    Button btnHostPaymentRoom;
    Button btnJoinPaymentRoom;
    TextView userNametext;
    TextView userFundsText;
    private UserRepository userRepository;
    private int hostid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        btnHostPaymentRoom = findViewById(R.id.btnHost);
        userNametext = findViewById(R.id.userUsername);
        userFundsText = findViewById(R.id.userFunds);


        userRepository = new UserRepository();
        userRepository.getUserByEmail("admin@mail.com", new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                userNametext.setText(user.getName());
                userFundsText.setText(String.format("%.2f €",user.getFunds()));
                hostid=user.getId();
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error cargando usuario " + "admin@mail.com" + ": " + message);
            }
        });
        //User user = repository.getUserByEmail("admin@mail.com");

        btnHostPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PreHostRoomActivity.class);
            intent.putExtra("ACTUAL_USER", hostid);
            startActivity(intent);
        });

        btnJoinPaymentRoom = findViewById(R.id.btnJoin);

        btnJoinPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PrePaymentJoinRoomActivity.class);
            intent.putExtra("ACTUAL_USER", hostid);
            startActivity(intent);
        });
    }
}