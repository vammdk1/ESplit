package com.tfm.es_plit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tfm.es_plit.R;
import com.tfm.es_plit.dataSimulation.fakeBackend;
import com.tfm.es_plit.dataSimulation.fakeUsers;
import com.tfm.es_plit.models.Participant;

import java.util.ArrayList;
import java.util.List;

public class PaymentPostHostRoomActivity extends AppCompatActivity {
    //Boton para cancelar proceso
    private Button btCancel;
    private Button btNextActivity;
    private List<Participant> plist = new ArrayList<>();
    private fakeBackend backend;
    private int tempPayment;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posthostroom);

        btCancel = findViewById(R.id.btnHostCancel);
        btNextActivity = findViewById(R.id.btnNextActivity);

        //recibir lista de deudores
        plist = (ArrayList<Participant>) getIntent().getSerializableExtra("pList");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT",0.0);

        //Objeto que almacena los ids de los invitados NFC
        //Semáforo secundairo que busca IDs

        //iniciar el backend
        backend = new fakeBackend(new fakeUsers(this));

        //crear pago
        tempPayment=backend.createTemporaryElement(plist,totalAmount);

        //botones
        btCancel.setOnClickListener( view -> {
            finish();
        });

        btNextActivity.setOnClickListener(v -> {
            if(backend.payment(tempPayment,totalAmount)){
                Intent intent = new Intent(PaymentPostHostRoomActivity.this, UserAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            };

        });
    }
}