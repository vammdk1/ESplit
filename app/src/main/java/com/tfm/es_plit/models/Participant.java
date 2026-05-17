package com.tfm.es_plit.models;

public class Participant {
    private final String name;
    private double amount;

    public Participant(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() { return name; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        this.amount=amount;
    }
}