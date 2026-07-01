package com.tfm.es_plit.activities.host;

import android.content.Intent;
import android.nfc.NfcAdapter;
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
import com.tfm.es_plit.nfc.NfcReaderHelper;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


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
    private NfcAdapter nfcAdapter;
    private NfcReaderHelper nfcReaderHelper;

    private void calcularMontos() {
        int numParticipants = plist.size();
        amounPerPerson = totalAmount / numParticipants ;

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
            if (p.getid() == hostId) {
                continue; // El host no necesita confirmar
            }
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
        hostStringAmmount.setText(String.format("%.2f €",totalAmount));
        splitAmmount = findViewById(R.id.totalSplitAmmount);
        splitAmmount.setText(String.format("%.2f €", totalAmount));

        userRepository = new UserRepository();
        paymentRepository = new PaymentRepository();
        socket = new PaymentSocket();

        //Añadir al host a la sala de pago en el backend para que lo puedan ver los participantes
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


        //vista de participantes
        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Adaptador para actuializar vistas despuésde iniciar NFC
        adapter = new ParticipantAdapter(plist, hostId, new ParticipantAdapter.OnParticipantActionListener() {
            @Override
            public void onRemove(Participant participant) {
                // Proceso para borrar del backend a un usuario de la sala de pago
                paymentRepository.removeParticipant(paymentId, participant.getid(),
                        new PaymentRepository.RemoveParticipantCallback() {
                            @Override
                            public void onSuccess() {
                                // notifica al participante que ha sido eliminado, web socket
                                try {
                                    JSONObject msg = new JSONObject();
                                    msg.put("type", "participant_removed");
                                    msg.put("user_id", participant.getid());
                                    socket.send(msg);
                                } catch (Exception e) {
                                    Log.e("WS", "Error notificando eliminación: " + e.getMessage());
                                }

                                // Acttualizar interfaz gráfica
                                runOnUiThread(() -> {
                                    calcularMontos();
                                    adapter.notifyDataSetChanged();
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Log.e("API", "Error eliminando participante: " + message);
                            }
                        });
                }
            @Override
            public void onConfirm(Participant participant) {
            }
        }, socket);

        recyclerView.setAdapter(adapter);

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
                    } else if ("participant_left".equals(message.getString("type"))) {
                        int userId = message.getInt("user_id");

                        runOnUiThread(() -> {
                            for (Participant p : plist) {
                                if (p.getid() == userId) {
                                    plist.remove(p);
                                    calcularMontos();
                                    break;
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

        //OBJETO NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcReaderHelper = new NfcReaderHelper(new NfcReaderHelper.NfcReadCallback() {
            @Override
            public void onUserIdRead(int userId) {
                userRepository.getUserById(userId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        for (Participant p : plist) {
                            if (p.getid() == user.getId()) return;
                        }
                        Participant participant = new Participant(user.getId(), user.getName());
                        plist.add(participant);

                        paymentRepository.addParticipant(paymentId, participant, new PaymentRepository.AddParticipantCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    calcularMontos();
                                    if (adapter != null) adapter.notifyDataSetChanged();
                                });
                            }
                            @Override
                            public void onError(String message) {
                                Log.e("API", "Error añadiendo participante NFC: " + message);
                            }
                        });
                    }
                    @Override
                    public void onError(String message) {
                        Log.e("NFC", "Error cargando usuario: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                String error = "Error con NFC, revisar paymenthost";
                Log.e("NFC", error);
            }
        });
        //TODO eliminar sala de pago del backend si el host cancela el pago.
        btCancel.setOnClickListener(view -> {
            //paymentRepository.
            finish();
        });

        // Botón para iniciar el pago, solo si todos los participantes han confirmado
        btStartpayment.setOnClickListener(v -> {
            if (paymentStatusCheck()) {
                Intent intent = new Intent(PaymentHostRoomActivity.this, PaymentPostHostRoomActivity.class);
                intent.putExtra("pList", (Serializable) plist);
                intent.putExtra("TOTAL_AMOUNT", totalAmount);
                intent.putExtra("PAYMENT_ID", paymentId);
                startActivity(intent);
            } else {
                Log.d("API", "No todos los participantes han confirmado el pago.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, nfcReaderHelper,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }
}