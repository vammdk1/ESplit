package com.tfm.es_plit.models;

public class ParticipantCreate {
    private int user_id;
    private String name;
    private double amount;

    public ParticipantCreate(int user_id, String name, double amount) {
        this.user_id = user_id;
        this.name = name;
        this.amount = amount;
    }
}