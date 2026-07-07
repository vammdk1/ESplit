package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.MainActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.activities.host.PreHostRoomActivity;
import com.tfm.es_plit.activities.participant.PrePaymentJoinRoomActivity;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.UserRepository;

public class UserAccountActivity extends AppCompatActivity {

    private Button btnHostPaymentRoom;
    private Button btnJoinPaymentRoom;
    private Button btnLogout;
    private TextView userNametext;
    private TextView userFundsText;
    private TextView userCardText;
    private UserRepository userRepository;
    private int hostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("USER", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        btnHostPaymentRoom = findViewById(R.id.btnHost);
        userNametext = findViewById(R.id.userUsername);
        userFundsText = findViewById(R.id.userFunds);
        userCardText = findViewById(R.id.userCard);

        // Inicializar repositorio y cargar usuario
        String token = new SessionManager(this).getToken();
        userRepository = new UserRepository(token);
        cargarDatosUser();
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
        btnLogout = findViewById(R.id.btnExit);
        btnLogout.setOnClickListener(v -> {
            userRepository.logout(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    // limpia la sesión local
                    new SessionManager(UserAccountActivity.this).clearSession();
                    // vuelve al login limpiando el stack
                    Intent intent = new Intent(UserAccountActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("API", "Error en logout: " + t.getMessage());
                }
            });
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("USER", "onResume");
        // Al volver de una actividad de pago, refrescar la información del usuario
        cargarDatosUser();
    }

    // Carga/actualiza la información del usuario en la pantalla
    private void cargarDatosUser() {
        // Asegurarse de tener el repositorio inicializado
        if (userRepository == null) {
            String token = new SessionManager(this).getToken();
            userRepository = new UserRepository(token);
        }

        // Actualiza el id del usuario (por si cambió la sesión)
        SessionManager session = new SessionManager(this);
        hostId = session.getUserId();

        userRepository.getUserById(hostId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                userNametext.setText(user.getName());
                userFundsText.setText(String.format("%.2f €", user.getFunds()));
                userCardText.setText(user.getCardNumber());
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error cargando usuario " + hostId + ": " + message);
            }
        });
    }
}