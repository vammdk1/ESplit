package com.tfm.es_plit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.activities.UserAccountActivity;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.UserRepository;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    EditText email;
    EditText pass;
    private UserRepository userRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnLogin);
        email = findViewById(R.id.loginEmail);
        pass = findViewById(R.id.loginPassword);

        btnStart.setOnClickListener(v -> {
            userRepository = new UserRepository();
            userRepository.getUserByEmail(email.getText().toString(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if(user.getPassword().equals(pass.getText().toString())){
                        SessionManager session = new SessionManager(MainActivity.this);
                        session.saveUserId(user.getId());

                        // guarda el card_number para el HCE
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("card_number", user.getCardNumber())
                                .apply();

                        Intent intent = new Intent(MainActivity.this, UserAccountActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e("API", "Error cargando usuario " + email.getText().toString() + ": " + message);

                }
            });


        });
    }
}