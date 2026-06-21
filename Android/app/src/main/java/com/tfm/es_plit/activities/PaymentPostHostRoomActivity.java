package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.network.PaymentRepository;

import java.util.ArrayList;
import java.util.List;

public class PaymentPostHostRoomActivity extends AppCompatActivity {

    private Button btCancel;
    private Button btNextActivity;
    private List<Participant> plist = new ArrayList<>();
    private PaymentRepository paymentRepository;
    private int tempPaymentId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posthostroom);
        btCancel = findViewById(R.id.btnHostCancel);
        btNextActivity = findViewById(R.id.btnNextActivity);

        plist = (ArrayList<Participant>) getIntent().getSerializableExtra("pList");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);

        paymentRepository = new PaymentRepository();

        paymentRepository.createPayment(totalAmount, plist, new PaymentRepository.CreatePaymentCallback() {
            @Override
            public void onSuccess(int paymentId) {
                tempPaymentId = paymentId;
                Log.d("API", "Payment creado con id " + paymentId);
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error creando payment: " + message);
            }
        });

        btCancel.setOnClickListener(view -> finish());

        btNextActivity.setOnClickListener(v -> {
            paymentRepository.pay(tempPaymentId, totalAmount, new PaymentRepository.PayCallback() {
                @Override
                public void onSuccess(boolean paymentStatus) {
                    if (paymentStatus) {
                        Intent intent = new Intent(PaymentPostHostRoomActivity.this, UserAccountActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e("API", "Error procesando pago: " + message);
                }
            });
        });
    }
}