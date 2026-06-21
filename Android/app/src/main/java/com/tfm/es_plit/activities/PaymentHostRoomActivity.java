package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfm.es_plit.R;
import com.tfm.es_plit.adapters.ParticipantAdapter;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.dataSimulation.fakeUsers;
import com.tfm.es_plit.models.Participant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaymentHostRoomActivity extends AppCompatActivity {
    //Boton para cancelar proceso
    private Button btCancel;
    private Button btStartpayment;
    private List<Participant> plist = new ArrayList<>();
    private double totalAmount;
    private double amounPerPerson;
    private TextView hostStringAmmount ;
    private double hostAmmount;
    private TextView splitAmmount ;
    private ParticipantAdapter adapter;

    //logica para dividir gastos equitatvamente
    private void calcularMontos(){
        int numParticipants = plist.size();
        amounPerPerson = totalAmount / (numParticipants+1);
        // Asignar la parte a cada participante
        for (Participant p : plist) {
            p.setAmount(amounPerPerson);
        }
        hostAmmount = amounPerPerson;
        hostStringAmmount.setText(String.format("%.2f €", amounPerPerson));
    };

    //Verifica si todos los participantes pueden pagar
    private boolean paymentStatusCheck(){
        for (Participant p: plist){
            if (!p.getConfirmationStatus()){
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

        //recibir dinero
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT",0.0);
        int hostId = getIntent().getIntExtra("ACTUAL_USER",0);
        hostStringAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);

        // Datos simulados
        splitAmmount.setText(String.format("%.2f €", totalAmount));
        fakeUsers repository = new fakeUsers(this);

        //Hacer bucle de lectura donde se reciben los IDs de los participantes
        //Objeto que almacena los ids de los invitados NFC
        //Semáforo secundairo que busca IDs

        //OBJETO NFC falso
        int[] Lids = {1,2};
        User tuser = repository.getUserById(Lids[0]);
        plist.add(new Participant(tuser.getId(),tuser.getName()));
        tuser = repository.getUserById(Lids[1]);
        plist.add(new Participant(tuser.getId(),tuser.getName()));

        // Dividir entre participantes
        calcularMontos();

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantAdapter(plist, new ParticipantAdapter.OnParticipantActionListener() {
            @Override
            public void onRemove(Participant participant) {
                calcularMontos();
                adapter.notifyDataSetChanged(); //actuliza los montos
            }

            @Override
            public void onConfirm(Participant participant) {

            }
        }, repository);

        recyclerView.setAdapter(adapter);

        //botones
        btCancel.setOnClickListener( view -> {
            finish();
        });

        btStartpayment.setOnClickListener(v -> {
            if (paymentStatusCheck()){
                Intent intent = new Intent(PaymentHostRoomActivity.this,PaymentPostHostRoomActivity.class);
                User hUser = repository.getUserById(hostId);
                Participant hostParticipant = new Participant(hUser.getId(),hUser.getName());
                hostParticipant.setAmount(hostAmmount);
                plist.add(hostParticipant);
                intent.putExtra("pList", (Serializable) plist);
                intent.putExtra("TOTAL_AMOUNT", totalAmount);
                startActivity(intent);
            }
        });
    }
}