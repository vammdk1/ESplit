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
    private int hostId;

    private void calcularMontos() {
        int numParticipants = plist.size();
        amounPerPerson = totalAmount / (numParticipants + 1);
        for (Participant p : plist) {
            p.setAmount(amounPerPerson);
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
        hostStringAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);
        splitAmmount.setText(String.format("%.2f €", totalAmount));

        userRepository = new UserRepository();

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //OBJETO NFC falso
        int[] Lids = {1, 2};
        AtomicInteger pending = new AtomicInteger(Lids.length);

        for (int id : Lids) {
            userRepository.getUserById(id, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    plist.add(new Participant(user.getId(), user.getName()));
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

                            adapter = new ParticipantAdapter(plist, new ParticipantAdapter.OnParticipantActionListener() {
                                @Override
                                public void onRemove(Participant participant) {
                                    calcularMontos();
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onConfirm(Participant participant) {
                                }
                            }, userRepository);

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

                        Intent intent = new Intent(PaymentHostRoomActivity.this, PaymentPostHostRoomActivity.class);
                        intent.putExtra("pList", (Serializable) plist);
                        intent.putExtra("TOTAL_AMOUNT", totalAmount);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("API", "Error cargando host: " + message);
                    }
                });
            }
        });
    }
}