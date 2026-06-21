package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.network.PaymentRepository;

public class PreHostRoomActivity extends AppCompatActivity {
    Button btnStart;
    Button btnCancel;
    PaymentRepository paymentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prehostroom);
        btnStart = findViewById(R.id.btnNextActivity);
        btnCancel = findViewById(R.id.btnHostCancel);
        paymentRepository = new PaymentRepository();

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
                btnStart.setEnabled(false); // evita doble click mientras se crea el payment

                paymentRepository.createEmptyPayment(amount, new PaymentRepository.CreatePaymentCallback() {
                    @Override
                    public void onSuccess(int paymentId) {
                        Intent intent = new Intent(PreHostRoomActivity.this, PaymentHostRoomActivity.class);
                        intent.putExtra("ACTUAL_USER", hostId);
                        intent.putExtra("TOTAL_AMOUNT", amount);
                        intent.putExtra("PAYMENT_ID", paymentId);
                        startActivity(intent);
                        btnStart.setEnabled(true);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("API", "Error creando payment: " + message);
                        btnStart.setEnabled(true);
                    }
                });
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}