package com.tfm.es_plit.network;

import com.tfm.es_plit.models.Participant;

import java.util.List;
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

    public interface PayCallback {
        void onSuccess(boolean paymentStatus);
        void onError(String message);
    }

    public void createPayment(double totalAmount, List<Participant> participants, CreatePaymentCallback callback) {
        PaymentCreate body = new PaymentCreate(totalAmount, participants);
        apiService.createPayment(body).enqueue(new Callback<PaymentResponse>() {
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
}