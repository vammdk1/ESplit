package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;
import com.tfm.es_plit.activities.host.PreHostRoomActivity;
import com.tfm.es_plit.activities.participant.PrePaymentJoinRoomActivity;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.UserRepository;

public class UserAccountActivity extends AppCompatActivity {

    Button btnHostPaymentRoom;
    Button btnJoinPaymentRoom;
    TextView userNametext;
    TextView userFundsText;
    TextView userCardText;
    private UserRepository userRepository;
    private int hostId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        btnHostPaymentRoom = findViewById(R.id.btnHost);
        userNametext = findViewById(R.id.userUsername);
        userFundsText = findViewById(R.id.userFunds);
        userCardText = findViewById(R.id.userCard);

        SessionManager session = new SessionManager(this);
        hostId = session.getUserId();

        userRepository = new UserRepository();
        userRepository.getUserById(hostId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                userNametext.setText(user.getName());
                userFundsText.setText(String.format("%.2f €",user.getFunds()));
                userCardText.setText(user.getCardNumber());
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error cargando usuario " + "admin@mail.com" + ": " + message);
            }
        });
        //User user = repository.getUserByEmail("admin@mail.com");

        btnHostPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PreHostRoomActivity.class);
            intent.putExtra("ACTUAL_USER", this.hostId);
            startActivity(intent);
        });

        btnJoinPaymentRoom = findViewById(R.id.btnJoin);

        btnJoinPaymentRoom.setOnClickListener(v -> {
            Intent intent = new Intent(UserAccountActivity.this, PrePaymentJoinRoomActivity.class);
            intent.putExtra("ACTUAL_USER", this.hostId);
            startActivity(intent);
        });
    }
}