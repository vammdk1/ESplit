package com.tfm.es_plit.activities.host;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.tfm.es_plit.R;
import com.tfm.es_plit.activities.UserAccountActivity;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.network.PaymentRepository;
import com.tfm.es_plit.network.PaymentSocket;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentPostHostRoomActivity extends AppCompatActivity {

    private Button btCancel;
    private Button btNextActivity;
    private List<Participant> plist = new ArrayList<>();
    private PaymentRepository paymentRepository;
    private PaymentSocket socket;
    private int paymentId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posthostroom);
        btCancel = findViewById(R.id.btnHostCancel);
        btNextActivity = findViewById(R.id.btnNextActivity);

        plist = (ArrayList<Participant>) getIntent().getSerializableExtra("pList");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        paymentId = getIntent().getIntExtra("PAYMENT_ID", 0);

        paymentRepository = new PaymentRepository();
        socket = new PaymentSocket();

        // conecta a la sala para poder hacer broadcast
        socket.connect(paymentId, new PaymentSocket.SocketListener() {
            @Override
            public void onConnected() {
                Log.d("WS", "PostHost conectado a la sala " + paymentId);
            }

            @Override
            public void onMessage(JSONObject message) {
                // revisar si hace falta usar la respuesta
            }

            @Override
            public void onError(String error) {
                Log.e("WS", "Error de conexión: " + error);
            }
        });


        btNextActivity.setOnClickListener(v -> {
            paymentRepository.pay(paymentId, totalAmount, new PaymentRepository.PayCallback() {
                @Override
                public void onSuccess(boolean paymentStatus) {
                    if (paymentStatus) {
                        //Notificar del fin de pago a todos los participantes de un pago con un broadcast
                        try {
                            JSONObject mesg = new JSONObject();
                            mesg.put("type","payment_completed");
                            socket.send(mesg);
                        } catch (Exception e){
                            Log.e("WS", "Error enviando payment_completed: " + e.getMessage());
                        }

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

        btCancel.setOnClickListener(view -> finish());
    }
}