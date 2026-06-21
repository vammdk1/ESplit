package com.tfm.es_plit.network;

public class ParticipantAdd {
    private int user_id;
    private String name;
    private double amount;

    public ParticipantAdd(int user_id, String name, double amount) {
        this.user_id = user_id;
        this.name = name;
        this.amount = amount;
    }
}