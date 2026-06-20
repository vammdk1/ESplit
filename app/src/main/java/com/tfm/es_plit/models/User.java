package com.tfm.es_plit.models;

public class User {

    private int id;
    private String name;
    private String email;
    private double funds;
    private String password;

    public User(int id, String name, String email, double funds, String password){
        this.id = id;
        this.name=name;
        this.email=email;
        this.funds=funds;
        this.password= password;
    }

    public int getId(){ return id;}
    public String getName(){return  name;}
    public  String getEmail(){return email;}
    public double getFunds(){return funds;}
    public String getPassword(){return password;}
    public void reduceFunds(double amount){
        this.funds-= amount;
    }
}
