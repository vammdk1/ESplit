package com.tfm.es_plit.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tfm.es_plit.R;
import com.tfm.es_plit.adapters.JoinAdapter;
import com.tfm.es_plit.adapters.ParticipantAdapter;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.dataSimulation.fakeUsers;
import com.tfm.es_plit.models.Participant;

import java.util.ArrayList;
import java.util.List;

public class PaymentJoinRoomActivity extends AppCompatActivity {

    //Boton para cancelar proceso
    private Button btCancel;
    private List<Participant> plist = new ArrayList<>();
    private double totalAmmount;
    private double ammounPerPerson;
    private TextView hostAmmount ;
    private TextView splitAmmount ;
    private JoinAdapter adapter;

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
        setContentView(R.layout.activity_joinroom);

        btCancel = findViewById(R.id.btnHostCancel);

        //recibir dinero
        totalAmmount = getIntent().getDoubleExtra("TOTAL_AMOUNT",0.0);
        hostAmmount = findViewById(R.id.textUnmanagedAmmount);
        splitAmmount = findViewById(R.id.totalSplitAmmount);

        // Datos simulados
        splitAmmount.setText(String.format("%.2f €", totalAmmount));
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

        RecyclerView recyclerView = findViewById(R.id.recyclerParticipantsJoin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //TODO crear nuevo reciclerview sin botones
        adapter = new JoinAdapter(plist,repository);
        recyclerView.setAdapter(adapter);

        //botones
        btCancel.setOnClickListener( view -> {
            finish();
        });
    }
}