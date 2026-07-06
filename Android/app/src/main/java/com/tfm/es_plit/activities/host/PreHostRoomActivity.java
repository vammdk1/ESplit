package com.tfm.es_plit.activities.host;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.PaymentRepository;
import com.tfm.es_plit.network.UserRepository;

import java.io.Serializable;

public class PreHostRoomActivity extends AppCompatActivity {
    Button btnStart;
    Button btnCancel;
    PaymentRepository paymentRepository;
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prehostroom);

        btnStart = findViewById(R.id.btnNextActivity);
        btnCancel = findViewById(R.id.btnHostCancel);
        String token = new SessionManager(this).getToken();
        paymentRepository = new PaymentRepository(token);
        userRepository = new UserRepository(token);

        //recibir host
        int hostId = getIntent().getIntExtra("ACTUAL_USER", 0);

        btnStart.setOnClickListener(v -> {
            EditText textAmount = findViewById(R.id.textAmountToPay);
            String input = textAmount.getText().toString();
            if (input.isEmpty()) {
                textAmount.setError("Introduce el monto a pagar");
                return;
            }
            double amount = Double.parseDouble(input);
            if (amount > 0) {
                btnStart.setEnabled(false); // bloquea posibles doble click mientras se crea el payment
                crearSalaConHost(hostId, amount);
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void crearSalaConHost(int hostId, double amount) {
        // convertiro el host actual a un participante y crear la sala de pago vacía en el backend
        userRepository.getUserById(hostId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User hUser) {
                Participant hostParticipant = new Participant(hUser.getId(), hUser.getName());
                hostParticipant.setAmount(amount); // de momento el host asume el total, hasta que se una alguien más
                hostParticipant.setConfirmationStatus(true); // el host no necesita confirmar

                // sala de pago vacía
                paymentRepository.createEmptyPayment(amount, new PaymentRepository.CreatePaymentCallback() {
                    @Override
                    public void onSuccess(int paymentId) {
                        // Añadir al host como participante en el backend
                        paymentRepository.addParticipant(paymentId, hostParticipant,
                                new PaymentRepository.AddParticipantCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Intent intent = new Intent(PreHostRoomActivity.this, PaymentHostRoomActivity.class);
                                        intent.putExtra("ACTUAL_USER", hostId);
                                        intent.putExtra("TOTAL_AMOUNT", amount);
                                        intent.putExtra("PAYMENT_ID", paymentId);
                                        intent.putExtra("HOST_PARTICIPANT", (Serializable) hostParticipant);
                                        startActivity(intent);
                                        btnStart.setEnabled(true);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e("API", "Error añadiendo host a la sala: " + message);
                                        btnStart.setEnabled(true);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("API", "Error creando payment: " + message);
                        btnStart.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error cargando datos del host: " + message);
                btnStart.setEnabled(true);
            }
        });
    }

}