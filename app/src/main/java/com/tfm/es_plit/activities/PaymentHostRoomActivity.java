package com.tfm.es_plit.activities;

import android.os.Bundle;

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

        // Datos simulados
        List<Participant> mockList = new ArrayList<>();
        mockList.add(new Participant("María", 25.50));
        mockList.add(new Participant("Carlos", 25.50));
        mockList.add(new Participant("Ana", 25.50));

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ParticipantAdapter(mockList));
    }
}