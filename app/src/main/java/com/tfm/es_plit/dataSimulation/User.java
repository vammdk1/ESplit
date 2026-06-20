package com.tfm.es_plit.dataSimulation;

public class User {

    private int id;
    private String name;
    private String email;
    private double funds;

    public User(int id, String name, String email, double funds){
        this.id = id;
        this.name=name;
        this.email=email;
        this.funds=funds;
    }

    public int getId(){ return id;}
    public String getName(){ return  name;}
    public  String getEmail(){return email;}
    public double getFunds(){ return funds;}
    public void addFunds(double amount){
        this.funds+= amount;
    }
    public void reduceFunds(double amount){
        this.funds-= amount;
    }

}
