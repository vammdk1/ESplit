package com.tfm.es_plit.network;

import com.tfm.es_plit.models.Participant;
import java.util.List;

public class PaymentCreate {
    private double total_amount;
    private List<ParticipantPayload> participants;

    public PaymentCreate(double totalAmount, List<Participant> participants) {
        this.total_amount = totalAmount;
        this.participants = new java.util.ArrayList<>();
        for (Participant p : participants) {
            this.participants.add(new ParticipantPayload(p.getid(), p.getName(), p.getAmount()));
        }
    }

    // clase interna, solo existe para mandar el JSON, no se usa en ningún otro lado
    private static class ParticipantPayload {
        int user_id;
        String name;
        double amount;

        ParticipantPayload(int user_id, String name, double amount) {
            this.user_id = user_id;
            this.name = name;
            this.amount = amount;
        }
    }
}