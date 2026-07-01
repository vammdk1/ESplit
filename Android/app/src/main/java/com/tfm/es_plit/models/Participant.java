package com.tfm.es_plit.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Participant implements Serializable {
    private final String name;
    @SerializedName("user_id")
    private final int id;
    private double amount;
    private boolean confirmationStatus;

    public Participant(int id,String name) {
        this.id = id;
        this.name = name;
        this.confirmationStatus = false;
    }

    public String getName() { return name; }
    public int getid(){ return id;}
    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        this.amount=amount;
    }
    public boolean getConfirmationStatus(){return this.confirmationStatus;}
    public void setConfirmationStatus(boolean newstatus){this.confirmationStatus=newstatus;}

}