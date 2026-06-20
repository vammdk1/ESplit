package com.tfm.es_plit.dataSimulation;

import com.tfm.es_plit.models.Participant;

import java.util.List;
import java.util.UUID;

public class fakeTempObject {

    private double totalAmount;
    private List<Participant> participantList;
    private int id;
    private boolean paymentStatus;

    public fakeTempObject(int id, double totalAmount, List<Participant> pList){
        this.id = id;
        this.totalAmount=totalAmount;
        this.participantList = pList;
        this.paymentStatus = false;
    }

    public int getId(){return this.id;}
    public boolean getPaymentStatus(){return this.paymentStatus;}
    public void setPaymentStatus(boolean newStatus){this.paymentStatus=newStatus;}
    public List<Participant> getAllParticipants(){return this.participantList;}
    public double getTotalAmount(){return this.totalAmount;}
    //Crear fecha de creación para medir el tiempo

}
