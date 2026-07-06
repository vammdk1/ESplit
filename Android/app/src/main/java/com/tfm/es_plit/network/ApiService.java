package com.tfm.es_plit.network;
import com.tfm.es_plit.models.Participant;
import com.tfm.es_plit.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
    @GET("payments/{id}")
    Call<PaymentDetail> getPaymentDetail(@Path("id") int paymentId);
    @POST("payments/")
    Call<PaymentResponse> createEmptyPayment(@Body PaymentCreateEmpty payment);
    @POST("payments/{id}/participants")
    Call<Object> addParticipant(@Path("id") int paymentId, @Body ParticipantAdd participant);
    @POST("payments/{id}/pay")
    Call<Map<String, Object>> pay(@Path("id") int id, @Query("amount_to_pay") double amount);
    @PUT("payments/{paymentId}/participants/{userId}")
    Call<Object> updateParticipantAmount(@Path("paymentId") int paymentId, @Path("userId") int userId, @Body ParticipantUpdate body);
    @DELETE("payments/{paymentId}/participants/{userId}")
    Call<Object> removeParticipant(@Path("paymentId") int paymentId, @Path("userId") int userId);
    @DELETE("payments/{id}")
    Call<Object> destroyPaymentRoom(@Path("id") int paymentId);
    @GET ("payments/{id}/participants")
    Call<List<Participant>> getPaymentRoomParticipants(@Path("id") int paymentId);
    @GET("users/by-card/{card_number}")
    Call<Map<String, Object>> getUserByCard(@Path("card_number") String cardNumber);
    @POST("users/login")
    Call<LoginResponse> login(@Query("email") String email, @Query("password") String password);
}
