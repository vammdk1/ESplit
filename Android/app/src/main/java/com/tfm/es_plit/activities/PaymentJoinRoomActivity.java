package com.tfm.es_plit.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfm.es_plit.R;
import com.tfm.es_plit.adapters.JoinAdapter;
import com.tfm.es_plit.network.UserRepository;
import com.tfm.es_plit.network.PaymentSocket;
import com.tfm.es_plit.models.Participant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentJoinRoomActivity extends AppCompatActivity {
    private Button btCancel;
    private List<Participant> plist = new ArrayList<>();
    private double totalAmmount;
    private TextView hostAmmount;
    private TextView splitAmmount;
    private JoinAdapter adapter;
    private UserRepository userRepository;
    private PaymentSocket socket;
    private int currentUserId;
    private int paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinroom);
        btCancel = findViewById(R.id.btnHostCancel);

        currentUserId = getIntent().getIntExtra("ACTUAL_USER", 0); // usuario logueado

        hostAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);

        userRepository = new UserRepository();
        socket = new PaymentSocket();

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipantsJoin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Simula el "justo después del handshake NFC": pregunta si hay invitación
        userRepository.getPendingPayment(currentUserId, new UserRepository.PendingPaymentCallback() {
            @Override
            public void onSuccess(boolean hasInvitation, int receivedPaymentId, double amount) {
                if (!hasInvitation) {
                    Log.d("API", "No hay invitación pendiente para: "+currentUserId);
                    return;
                }

                paymentId = receivedPaymentId;
                totalAmmount = amount;

                runOnUiThread(() -> {
                    splitAmmount.setText(String.format("%.2f €", totalAmmount));
                    hostAmmount.setText(String.format("%.2f €", totalAmmount));

                    adapter = new JoinAdapter(plist, socket);
                    recyclerView.setAdapter(adapter);

                    connectSocket();
                });
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error consultando invitación: " + message);
            }
        });

        btCancel.setOnClickListener(view -> finish());
    }

    private void connectSocket() {
        socket.connect(paymentId, new PaymentSocket.SocketListener() {
            @Override
            public void onConnected() {
                Log.d("WS", "Participante conectado a la sala " + paymentId);
            }

            @Override
            public void onMessage(JSONObject message) {
                try {
                    if ("confirm_request".equals(message.getString("type"))
                            && message.getInt("user_id") == currentUserId) {

                        double amount = message.getDouble("amount");

                        runOnUiThread(() -> showConfirmDialog(amount));
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
    }

    private void showConfirmDialog(double amount) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar pago")
                .setMessage("El host solicita tu confirmación para pagar " + String.format("%.2f €", amount))
                .setPositiveButton("Aceptar", (dialog, which) -> sendResponse(true))
                .setNegativeButton("Rechazar", (dialog, which) -> sendResponse(false))
                .setCancelable(false)
                .show();
    }

    private void sendResponse(boolean accepted) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "confirm_response");
            response.put("user_id", currentUserId);
            response.put("accepted", accepted);
            socket.send(response);
        } catch (Exception e) {
            Log.e("WS", "Error enviando respuesta: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
    }
}