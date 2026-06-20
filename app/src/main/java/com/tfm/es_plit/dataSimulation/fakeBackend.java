package com.tfm.es_plit.dataSimulation;

import android.util.Log;

import com.tfm.es_plit.models.Participant;

import java.util.List;

public class fakeBackend {
    public String createTemporaryElement(List<Participant> participants) {


        // simula guardar y generar un id
        String fakeId = "temp-" + System.currentTimeMillis();
        Log.d("FAKE_BACKEND", "Creado elemento " + fakeId + " con " + participants.size() + " participantes");
        return fakeId;
    }
}
