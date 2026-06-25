package com.tfm.es_plit.network;

import java.util.List;

public class PaymentDetail {
    private int id;
    private double total_amount;
    private boolean payment_status;
    private List<ParticipantDetail> participants;

    public int getId() { return id; }
    public double getTotalAmount() { return total_amount; }
    public boolean getPaymentStatus() { return payment_status; }
    public List<ParticipantDetail> getParticipants() { return participants; }

    public static class ParticipantDetail {
        private int user_id;
        private String name;
        private double amount;
        private boolean confirmation_status;

        public int getUserId() { return user_id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public boolean getConfirmationStatus() { return confirmation_status; }
    }
}