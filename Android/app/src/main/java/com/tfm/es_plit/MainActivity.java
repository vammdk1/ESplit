package com.tfm.es_plit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.activities.UserAccountActivity;
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
                        Intent intent = new Intent(MainActivity.this, UserAccountActivity.class);
                        intent.putExtra("ACTUAL_USER", user.getId());
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