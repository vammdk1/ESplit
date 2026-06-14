package com.tfm.es_plit.activities;

import android.os.Bundle;
import android.widget.Button;
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
    //Boton para cancelar proceso
    private Button btCancel;
    private final List<Participant> plist = new ArrayList<>();
    private double totalAmmount;
    private double ammounPerPerson;
    private TextView hostAmmount ;
    private TextView splitAmmount ;
    private ParticipantAdapter adapter;

    //logica para dividir gastos equitatvamente
    private void calcularMontos(){
        int numParticipants = plist.size();
        ammounPerPerson = totalAmmount / (numParticipants+1);

        // Asignar la parte a cada participante
        for (Participant p : plist) {
            p.setAmount(ammounPerPerson);
        }

        hostAmmount.setText(String.format("%.2f €", ammounPerPerson));
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostroom);

        btCancel = findViewById(R.id.btnHostCancel);

        //recibir dinero
        totalAmmount = getIntent().getDoubleExtra("TOTAL_AMOUNT",0.0);
        hostAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);

        // Datos simulados
        splitAmmount.setText(String.format("%.2f €", totalAmmount));
        plist.add(new Participant("María", 0.0));
        plist.add(new Participant("Carlos", 0.0));
        plist.add(new Participant("Ana", 0.0));

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
                //TODO enviar mensaje de confirmación de monto
            }
        });
        recyclerView.setAdapter(adapter);

        //botones
        btCancel.setOnClickListener( view -> {
            finish();
        });
    }
}