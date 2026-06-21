package com.tfm.es_plit.network;

import com.tfm.es_plit.models.Participant;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private final ApiService apiService;

    public PaymentRepository() {
        this.apiService = ApiClient.getInstance().create(ApiService.class);
    }

    public interface CreatePaymentCallback {
        void onSuccess(int paymentId);
        void onError(String message);
    }

    public interface AddParticipantCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface PayCallback {
        void onSuccess(boolean paymentStatus);
        void onError(String message);
    }

    public interface UpdateParticipantCallback {
        void onSuccess();
        void onError(String message);
    }

    public void createEmptyPayment(double totalAmount, CreatePaymentCallback callback) {
        PaymentCreateEmpty body = new PaymentCreateEmpty(totalAmount);
        apiService.createEmptyPayment(body).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getId());
                } else {
                    callback.onError("Error al crear el pago");
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void addParticipant(int paymentId, Participant p, AddParticipantCallback callback) {
        ParticipantAdd body = new ParticipantAdd(p.getid(), p.getName(), p.getAmount());
        apiService.addParticipant(paymentId, body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al añadir participante");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void pay(int paymentId, double amount, PayCallback callback) {
        apiService.pay(paymentId, amount).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.TRUE.equals(response.body().get("success"));
                    callback.onSuccess(success);
                } else {
                    callback.onError("Error al procesar el pago");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    public void updateParticipantAmount(int paymentId, int userId, double amount, UpdateParticipantCallback callback) {
        ParticipantUpdate body = new ParticipantUpdate(userId, amount);
        apiService.updateParticipantAmount(paymentId, userId, body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error actualizando monto");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}