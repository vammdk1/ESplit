package com.tfm.es_plit.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.194:8000/";

    private static Retrofit retrofit;
    //convierte los mensajes JSON en objetos java y viceversa
    public static Retrofit getInstance(String token) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(); // Interceptor para registrar las solicitudes y respuestas HTTP
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // registra el cuerpo de las solicitudes y respuestas

        // Agrega un interceptor para agregar el token de autorización a cada solicitud
        // el interceptor sirve para modificar las solicitudes antes de enviarlas y las respuestas antes de recibirlas
        // como el token es necesario para acceder a los endpoints protegidos, se agrega a cada solicitud
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        //convierte el JSON en clases según la llamada a la API
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}