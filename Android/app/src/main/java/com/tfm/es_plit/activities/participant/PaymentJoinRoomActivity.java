package com.tfm.es_plit.activities.participant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfm.es_plit.R;
import com.tfm.es_plit.activities.UserAccountActivity;
import com.tfm.es_plit.adapters.ParticipantAdapter;
import com.tfm.es_plit.data.SessionManager;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.PaymentRepository;
import com.tfm.es_plit.network.UserRepository;
import com.tfm.es_plit.network.PaymentSocket;
import com.tfm.es_plit.models.Participant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PaymentJoinRoomActivity extends AppCompatActivity {
    private Button btCancel;
    private List<Participant> plist = new ArrayList<>();
    private double totalAmmount;
    private double participantAmmount;
    private TextView localHostAmmount;
    private TextView splitAmmount;
    private ParticipantAdapter adapter;
    private UserRepository userRepository;
    private PaymentRepository paymentRepository;

    private PaymentSocket socket;
    private int currentUserId;
    private int paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinroom);
        btCancel = findViewById(R.id.btnHostCancel);

        currentUserId = new SessionManager(this).getUserId();

        localHostAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);

        paymentRepository = new PaymentRepository();
        userRepository = new UserRepository();
        socket = new PaymentSocket();

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipantsJoin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        paymentId = getIntent().getIntExtra("PAYMENT_ID", 0);
        totalAmmount = getIntent().getDoubleExtra("FULL_AMOUNT", 0.0);
        participantAmmount = getIntent().getDoubleExtra("PARTICIPANT_AMOUNT", 0.0);

        splitAmmount.setText(String.format("%.2f €", totalAmmount));
        localHostAmmount.setText(String.format("%.2f €", participantAmmount));

        adapter = new ParticipantAdapter(plist,currentUserId, false , new ParticipantAdapter.OnParticipantActionListener() {
            @Override
            public void onRemove(Participant participant) {
                // No se permite eliminar a otros participantes desde la vista del participante
            }
            @Override
            public void onConfirm(Participant participant) {
                // No se permite confirmar a otros participantes desde la vista del participante
            }
        }, socket);
        recyclerView.setAdapter(adapter);

        paymentRepository.getPaymentRoom(paymentId, new PaymentRepository.getPaymentRoomParticipantsCallback() {
            @Override
            public void onSuccess(List<Participant> participants) {
                plist.clear();
                plist.addAll(participants);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error consultando participantes: " + message);
            }
        });
        connectSocket();


        btCancel.setOnClickListener(view -> {
            onRemoveParticipant();
            Intent intent = new Intent(PaymentJoinRoomActivity.this, UserAccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
    //El participante decide salirse de la sala, eliminar del backend y actualizar a los demás participantes
    private void onRemoveParticipant() {
        paymentRepository.removeParticipant(paymentId, currentUserId, new PaymentRepository.RemoveParticipantCallback() {
            @Override
            public void onSuccess() {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "participant_left");
                    msg.put("user_id", currentUserId);
                    socket.send(msg);
                } catch (JSONException e) {
                    Log.e("API", "Error creando mensaje: " + e.getMessage());
                }
                Log.d("API", "Participante eliminado de la sala");
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error eliminando participante: " + message);
            }
        });
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
                    String type = message.getString("type");

                    if ("confirm_request".equals(type) && message.getInt("user_id") == currentUserId) {
                        double amount = message.getDouble("amount");
                        runOnUiThread(() -> showConfirmDialog(amount));

                    } else if ("amount_updated".equals(type) && message.getInt("user_id") == currentUserId) {
                        double newAmount = message.getDouble("amount");
                        runOnUiThread(() -> {
                            totalAmmount = newAmount;
                            localHostAmmount.setText(String.format("%.2f €", newAmount));
                        });

                    } else if ("payment_completed".equals(type)) {
                        runOnUiThread(() -> {
                            Log.d("WS", "Pago completado, cerrando sala");
                            Intent intent = new Intent(PaymentJoinRoomActivity.this, UserAccountActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    } else if ("participant_removed".equals(type) // eliminar usuario del pago
                            && message.getInt("user_id") == currentUserId) {
                        runOnUiThread(() -> {
                            showExpulsionDialog();
                        });
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
        userRepository.getUserById(currentUserId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getFunds() < amount) {
                    new androidx.appcompat.app.AlertDialog.Builder(PaymentJoinRoomActivity.this)
                            .setTitle("Fondos insuficientes")
                            .setMessage("No tienes suficiente saldo para participar en el pago.")
                            .setPositiveButton("Aceptar", (dialog, which) -> sendResponse(false))
                            .setCancelable(false)
                            .show();

                } else
                    new androidx.appcompat.app.AlertDialog.Builder(PaymentJoinRoomActivity.this)
                            .setTitle("Confirmar pago")
                            .setMessage("El host solicita tu confirmación para pagar " + String.format("%.2f €", amount))
                            .setPositiveButton("Aceptar", (dialog, which) -> sendResponse(true))
                            .setNegativeButton("Rechazar", (dialog, which) -> sendResponse(false))
                            .setCancelable(false)
                            .show();
            }

            @Override
            public void onError(String message) {
                Log.e("API", "Error obteniendo datos del usuario: " + message);
            }
        });
    }

    private void showExpulsionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Expulsado de la sala")
                .setMessage("Has sido expulsado de la sala de pago por el host.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    Intent intent = new Intent(PaymentJoinRoomActivity.this, UserAccountActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
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