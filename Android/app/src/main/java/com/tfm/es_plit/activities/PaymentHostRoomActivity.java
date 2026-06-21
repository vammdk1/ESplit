package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tfm.es_plit.R;
import com.tfm.es_plit.adapters.ParticipantAdapter;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.network.UserRepository;
import com.tfm.es_plit.network.PaymentRepository;
import com.tfm.es_plit.network.PaymentSocket;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentHostRoomActivity extends AppCompatActivity {
    private Button btCancel;
    private Button btStartpayment;
    private List<Participant> plist = new ArrayList<>();
    private double totalAmount;
    private double amounPerPerson;
    private TextView hostStringAmmount;
    private double hostAmmount;
    private TextView splitAmmount;
    private ParticipantAdapter adapter;
    private UserRepository userRepository;
    private PaymentRepository paymentRepository;
    private PaymentSocket socket;
    private int hostId;
    private int paymentId;

    private void calcularMontos() {
        int numParticipants = plist.size();
        amounPerPerson = totalAmount / (numParticipants + 1);

        for (Participant p : plist) {
            p.setAmount(amounPerPerson);
            p.setConfirmationStatus(false);

            // Actualiza en el backend
            paymentRepository.updateParticipantAmount(paymentId, p.getid(), amounPerPerson,
                    new PaymentRepository.UpdateParticipantCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("API", "Monto actualizado para " + p.getid());
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("API", "Error actualizando monto: " + message);
                        }
                    });

            // Notifica al participante en vivo por WebSocket
            try {
                JSONObject update = new JSONObject();
                update.put("type", "amount_updated");
                update.put("user_id", p.getid());
                update.put("amount", amounPerPerson);
                socket.send(update);
            } catch (Exception e) {
                Log.e("WS", "Error notificando cambio de monto: " + e.getMessage());
            }
        }

        hostAmmount = amounPerPerson;
        hostStringAmmount.setText(String.format("%.2f €", amounPerPerson));
    }

    private boolean paymentStatusCheck() {
        for (Participant p : plist) {
            if (!p.getConfirmationStatus()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostroom);
        btCancel = findViewById(R.id.btnHostCancel);
        btStartpayment = findViewById(R.id.btnHostPay);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        hostId = getIntent().getIntExtra("ACTUAL_USER", 0);
        paymentId = getIntent().getIntExtra("PAYMENT_ID", 0);

        hostStringAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);
        splitAmmount.setText(String.format("%.2f €", totalAmount));

        userRepository = new UserRepository();
        paymentRepository = new PaymentRepository();
        socket = new PaymentSocket();

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Abre el WebSocket de la sala ya creada
        socket.connect(paymentId, new PaymentSocket.SocketListener() {
            @Override
            public void onConnected() {
                Log.d("WS", "Host conectado a la sala " + paymentId);
            }

            @Override
            public void onMessage(JSONObject message) {
                try {
                    if ("confirm_response".equals(message.getString("type"))) {
                        int userId = message.getInt("user_id");
                        boolean accepted = message.getBoolean("accepted");

                        runOnUiThread(() -> {
                            for (Participant p : plist) {
                                if (p.getid() == userId) {
                                    p.setConfirmationStatus(accepted);
                                }
                            }
                            if (adapter != null) adapter.notifyDataSetChanged();
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

        //OBJETO NFC falso
        int[] Lids = {1, 3};
        AtomicInteger pending = new AtomicInteger(Lids.length);

        for (int id : Lids) {
            userRepository.getUserById(id, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    Participant participant = new Participant(user.getId(), user.getName());
                    plist.add(participant);
                    checkAllLoaded();
                }

                @Override
                public void onError(String message) {
                    Log.e("API", "Error cargando usuario " + id + ": " + message);
                    checkAllLoaded();
                }

                private void checkAllLoaded() {
                    if (pending.decrementAndGet() == 0) {
                        runOnUiThread(() -> {
                            calcularMontos();

                            // Registra cada participante en el backend
                            for (Participant p : plist) {
                                paymentRepository.addParticipant(paymentId, p, new PaymentRepository.AddParticipantCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("API", "Participante " + p.getid() + " añadido a la sala");
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e("API", "Error añadiendo participante: " + message);
                                    }
                                });
                            }

                            adapter = new ParticipantAdapter(plist, new ParticipantAdapter.OnParticipantActionListener() {
                                @Override
                                public void onRemove(Participant participant) {
                                    calcularMontos();
                                    //Eliminar todas las confirmaciones y actualizar el monto de todos los usarios que quedan
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onConfirm(Participant participant) {
                                }
                            }, socket);

                            recyclerView.setAdapter(adapter);
                        });
                    }
                }
            });
        }

        btCancel.setOnClickListener(view -> finish());

        btStartpayment.setOnClickListener(v -> {
            if (paymentStatusCheck()) {
                userRepository.getUserById(hostId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User hUser) {
                        Participant hostParticipant = new Participant(hUser.getId(), hUser.getName());
                        hostParticipant.setAmount(hostAmmount);
                        plist.add(hostParticipant);

                        paymentRepository.addParticipant(paymentId, hostParticipant, new PaymentRepository.AddParticipantCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("API", "Host añadido a la sala");

                                Intent intent = new Intent(PaymentHostRoomActivity.this, PaymentPostHostRoomActivity.class);
                                intent.putExtra("pList", (Serializable) plist);
                                intent.putExtra("TOTAL_AMOUNT", totalAmount);
                                intent.putExtra("PAYMENT_ID", paymentId);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(String message) {
                                Log.e("API", "Error añadiendo host: " + message);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("API", "Error cargando host: " + message);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
    }
}