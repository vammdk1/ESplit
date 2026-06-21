package com.tfm.es_plit.network;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;

public class PaymentSocket {
    private WebSocket webSocket;
    private final OkHttpClient client = new OkHttpClient();
    private final String baseWsUrl = "ws://192.168.1.194:8000/ws/payments/";

    public interface SocketListener {
        void onMessage(JSONObject message);
        void onConnected();
        void onError(String error);
    }

    public void connect(int paymentId, SocketListener listener) {
        Request request = new Request.Builder()
                .url(baseWsUrl + paymentId)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                listener.onConnected();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    listener.onMessage(json);
                } catch (Exception e) {
                    Log.e("PaymentSocket", "Error parseando mensaje: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WS", "onFailure: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                if (response != null) {
                    Log.e("WS", "Response code: " + response.code());
                }
                listener.onError(t.getMessage());
            }
        });
    }

    public void send(JSONObject message) {
        if (webSocket != null) {
            webSocket.send(message.toString());
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Activity cerrada");
        }
    }

}