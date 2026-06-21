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
    @GET("users/by-email/{email}")
    Call<User> getUserByEmail(@Path("email") String email);
    @GET("users/{id}/pending-payment")
    Call<Map<String,Object>>getPendingPayment(@Path("id") int userID);
    @POST("payments/")
    Call<PaymentResponse> createEmptyPayment(@Body PaymentCreateEmpty payment);

    @POST("payments/{id}/participants")
    Call<Object> addParticipant(@Path("id") int paymentId, @Body ParticipantAdd participant);

    @POST("payments/{id}/pay")
    Call<Map<String, Object>> pay(@Path("id") int id, @Query("amount_to_pay") double amount);

}
