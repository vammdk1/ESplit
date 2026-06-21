package com.tfm.es_plit.network;
import com.tfm.es_plit.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("users/")
    Call<List<User>> getUsers();

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") int id);

    @POST("payments/")
    Call<PaymentResponse> createPayment(@Body PaymentCreate payment);

    @POST("payments/{id}/pay")
    Call<Map<String, Object>> pay(@Path("id") int id, @Query("amount_to_pay") double amount);
}