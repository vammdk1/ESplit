package com.tfm.es_plit.network;

import com.tfm.es_plit.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService apiService;

    public UserRepository() {
        this.apiService = ApiClient.getInstance().create(ApiService.class);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface UsersListCallback {
        void onSuccess(List<User> users);
        void onError(String message);
    }
    public interface PendingPaymentCallback {
        void onSuccess(boolean hasInvitation, int paymentId, double amount);
        void onError(String message);
    }

    public void getPendingPayment(int userId, PendingPaymentCallback callback) {
        apiService.getPendingPayment(userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    boolean hasInvitation = Boolean.TRUE.equals(body.get("has_invitation"));
                    if (hasInvitation) {
                        int paymentId = ((Double) body.get("payment_id")).intValue();
                        double amount = (Double) body.get("amount");
                        callback.onSuccess(true, paymentId, amount);
                    } else {
                        callback.onSuccess(false, 0, 0.0);
                    }
                } else {
                    callback.onError("Error consultando invitación");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUserById(int id, UserCallback callback) {
        apiService.getUserById(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Usuario no encontrado");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    public void getUserByEmail(String email, UserCallback callback) {
        apiService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Usuario no encontrado");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getUsers(UsersListCallback callback) {
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener usuarios");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

}