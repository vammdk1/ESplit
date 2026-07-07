package com.tfm.es_plit.activities.host;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.activities.UserAccountActivity;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.network.PaymentRepository;
import com.tfm.es_plit.network.PaymentSocket;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentPostHostRoomActivity extends AppCompatActivity {

    private Button btCancel;
    private TextView timerText;
    private TextView totalAmmountText;
    private List<Participant> plist = new ArrayList<>();
    private PaymentRepository paymentRepository;
    private PaymentSocket socket;
    private int paymentId;
    private double totalAmount;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posthostroom);
        btCancel = findViewById(R.id.btnHostCancel);
        totalAmmountText = findViewById(R.id.StringPaymentAmount);
        timerText = findViewById(R.id.paymentTimer);

        plist = (ArrayList<Participant>) getIntent().getSerializableExtra("pList");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        paymentId = getIntent().getIntExtra("PAYMENT_ID", 0);

        totalAmmountText.setText(String.format("%.2f €", totalAmount));
        String token = new SessionManager(this).getToken();
        paymentRepository = new PaymentRepository(token);
        socket = new PaymentSocket();

        // conecta a la sala para poder hacer broadcast
        socket.connect(paymentId, token, new PaymentSocket.SocketListener() {
            @Override
            public void onConnected() {
                Log.d("WS", "PostHost conectado a la sala " + paymentId);
            }
            @Override
            public void onMessage(JSONObject message) {
                try {
                    if ("payment_completed".equals(message.getString("type"))) {
                        runOnUiThread(() -> navigateHome());
                    }
                } catch (Exception e) {
                    Log.e("WS", "Error procesando mensaje: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.e("WS", "Error de conexión: " + error);
            }
        });

        //Contador de 10 minutos
        timer = new CountDownTimer(10 * 60 * 1000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualiza la UI con el tiempo restante
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Acción a realizar cuando el contador llegue a cero
                timerText.setText("00:00");
                finish();
            }
        }.start();
        btCancel.setOnClickListener(view -> finish());
    }
    private void navigateHome() {
        Intent intent = new Intent(PaymentPostHostRoomActivity.this, UserAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activa el HCE para que el host emita su card_number al TPV
        getSharedPreferences("nfc_prefs", MODE_PRIVATE)
                .edit().putBoolean("hce_active", true).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // desactiva el HCE al salir
        getSharedPreferences("nfc_prefs", MODE_PRIVATE)
                .edit().putBoolean("hce_active", false).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        socket.close();
    }
}