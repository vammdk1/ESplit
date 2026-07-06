package com.tfm.es_plit.activities.participant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.network.UserRepository;

public class PrePaymentJoinRoomActivity extends AppCompatActivity {
    Button btnCancel;
    private UserRepository userRepository;
    private android.os.Handler pollingHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pollingRunnable;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prejoinroom);
        btnCancel = findViewById(R.id.btnHostCancel);
        String token = new SessionManager(this).getToken();
        userRepository = new UserRepository(token);
        currentUserId = new SessionManager(this).getUserId();

        // arranca el polling automáticamente al entrar a la pantalla
        buscarSalaConReintentos();

        btnCancel.setOnClickListener(v -> finish());
    }

    // metodo para buscar sala con reintentos y los datos de la sala
    private void buscarSalaConReintentos() {
        pollingRunnable = () -> {
            userRepository.getPendingPayment(currentUserId, new UserRepository.PendingPaymentCallback() {
                @Override
                public void onSuccess(boolean hasInvitation, int paymentId, double amount, double fullAmount) {
                    if (hasInvitation) {
                        // encontró sala, navega automáticamente
                        Intent intent = new Intent(PrePaymentJoinRoomActivity.this, PaymentJoinRoomActivity.class);
                        intent.putExtra("PAYMENT_ID", paymentId);
                        intent.putExtra("PARTICIPANT_AMOUNT", amount);
                        intent.putExtra("FULL_AMOUNT", fullAmount);
                        intent.putExtra("ACTUAL_USER",currentUserId);
                        startActivity(intent);
                    } else {
                        // reintenta en 3 segundos
                        pollingHandler.postDelayed(pollingRunnable, 3000);
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e("API", "Error consultando invitación: " + message);
                    pollingHandler.postDelayed(pollingRunnable, 3000);
                }
            });
        };
        pollingHandler.post(pollingRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollingHandler.removeCallbacks(pollingRunnable);
    }
    // Activa o desactiva el servicio HCE según el estado de la actividad
    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences("nfc_prefs", MODE_PRIVATE)
                .edit().putBoolean("hce_active", true).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("nfc_prefs", MODE_PRIVATE)
                .edit().putBoolean("hce_active", false).apply();
    }
}
