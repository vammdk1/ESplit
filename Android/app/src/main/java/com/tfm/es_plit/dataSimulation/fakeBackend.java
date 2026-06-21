package com.tfm.es_plit.dataSimulation;

import android.util.Log;

import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.models.User;

import java.util.List;
import java.util.Random;

public class fakeBackend {

    private fakeTempObject tempObject;
    private fakeUsers tUserRepo;
    private User tUser;

    public fakeBackend(fakeUsers userrepository){
        this.tUserRepo = userrepository;
    }
    public int createTemporaryElement(List<Participant> participants, double totalAmountm) {

        // simula guardar y generar un id
        int tempId = new Random().nextInt();
        tempObject = new fakeTempObject(tempId,totalAmountm,participants);
        Log.d("FAKE_BACKEND", "Creado elemento " + tempId + " con " + participants.size() + " participantes");
        return tempId;
    }

    public boolean payment(int paymentID, double amountToPay){
        if(tempObject.getId() ==paymentID){
            if (tempObject.getTotalAmount() == amountToPay){
                List<Participant> tempParticipantList = tempObject.getAllParticipants();
                Log.d("Fake_HandShake_Participant", "Participantes : " + tempParticipantList);
                for (Participant p: tempParticipantList) {
                    Log.d("Fake_HandShake_Participant", "Participante procesado: " + p.getid() +"| Monto participante: " + p.getAmount());
                    tUser = tUserRepo.getUserById(p.getid());
                    tUser.reduceFunds(p.getAmount());
                }
                tempObject.setPaymentStatus(true);
                return tempObject.getPaymentStatus();
            }
        }
        return tempObject.getPaymentStatus();
    }
}
