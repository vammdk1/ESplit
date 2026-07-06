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
            // el login no necesita token, se crea sin él
            userRepository = new UserRepository("");
            userRepository.login(email.getText().toString(), pass.getText().toString(), new UserRepository.LoginCallback() {
                @Override
                public void onSuccess(int userId, String token) {
                    SessionManager session = new SessionManager(MainActivity.this);
                    session.saveSession(userId, token);

                    UserRepository authenticatedRepo = new UserRepository(token);
                    authenticatedRepo.getUserById(userId, new UserRepository.UserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("card_number", user.getCardNumber())
                                    .apply();
                            Intent intent = new Intent(MainActivity.this, UserAccountActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("API", "Error cargando usuario: " + message);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e("API", "Error en login: " + message);
                }
            });
        });
    }
}