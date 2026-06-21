package com.tfm.es_plit.network;

public class ParticipantUpdate {
    private int user_id;
    private double amount;

    public ParticipantUpdate(int user_id, double amount) {
        this.user_id = user_id;
        this.amount = amount;
    }
}