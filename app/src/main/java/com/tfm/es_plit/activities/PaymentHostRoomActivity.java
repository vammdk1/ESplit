package com.tfm.es_plit.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfm.es_plit.R;
import com.tfm.es_plit.adapters.ParticipantAdapter;
import com.tfm.es_plit.models.Participant;

import java.util.ArrayList;
import java.util.List;

public class PaymentHostRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostroom);

        //recibir dinero
        double totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT",0.0);
        TextView hostAmount = findViewById(R.id.textUnmanagedAmmount);

        // Datos simulados
        List<Participant> mockList = new ArrayList<>();
        mockList.add(new Participant("María", 0.0));
        mockList.add(new Participant("Carlos", 0.0));
        mockList.add(new Participant("Ana", 0.0));

        // Dividir entre participantes
        int numParticipants = mockList.size();
        double amountPerPerson = totalAmount / (numParticipants+1);

        // Asignar la parte a cada participante
        for (Participant p : mockList) {
            p.setAmount(amountPerPerson);
        }
        hostAmount.setText(String.format("%.2f €", amountPerPerson));


        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ParticipantAdapter(mockList));
    }
}