package com.tfm.es_plit.models;

public class User {

    private int id;
    private String name;
    private String email;
    private double funds;
    private String password;
    private String card_number;

    public User(int id, String name, String email, double funds, String password, String cardNumber){
        this.id = id;
        this.name=name;
        this.email=email;
        this.funds=funds;
        this.password= password;
        this.card_number = cardNumber;
    }

    public int getId(){ return id;}
    public String getName(){return  name;}
    public  String getEmail(){return email;}
    public double getFunds(){return funds;}
    public String getPassword(){return password;}
    public String getCardNumber(){return card_number;}
    public void reduceFunds(double amount){
        this.funds-= amount;
    }
}
