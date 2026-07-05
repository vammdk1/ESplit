package com.tfm.esplittpv;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TPV";
    private static final String BASE_URL = "http://192.168.1.194:8000";

    private static final byte[] SELECT_AID_COMMAND = {
        (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
        (byte) 0x07,
        (byte) 0xF0, (byte) 0x39, (byte) 0x41, (byte) 0x48,
        (byte) 0x14, (byte) 0x81, (byte) 0x00
    };

    private NfcAdapter nfcAdapter;
    private TextView statusText;
    private EditText amountInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        amountInput = findViewById(R.id.amountInput);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        statusText.setText("Esperando tarjeta...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, tag -> {
                IsoDep isoDep = IsoDep.get(tag);
                if (isoDep == null) return;
                try {
                    isoDep.connect();
                    byte[] response = isoDep.transceive(SELECT_AID_COMMAND);
                    if (response != null && response.length > 2) {
                        byte[] data = new byte[response.length - 2];
                        System.arraycopy(response, 0, data, 0, data.length);
                        String cardNumber = new String(data).trim();
                        Log.d(TAG, "Tarjeta leída: " + cardNumber);
                        runOnUiThread(() -> statusText.setText("Tarjeta leída: " + cardNumber));
                        chargeCard(cardNumber);
                    }
                    isoDep.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error NFC: " + e.getMessage());
                    runOnUiThread(() -> statusText.setText("Error NFC: " + e.getMessage()));
                }
            },
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    private void chargeCard(String cardNumber) {
        new Thread(() -> {
        try {
            String amountStr = amountInput.getText().toString();
            if (amountStr.isEmpty()) {
                runOnUiThread(() -> statusText.setText("Introduce el monto primero"));
                return;
            }
            double amount = Double.parseDouble(amountStr);

            URL url = new URL(BASE_URL + "/payments/tpv/charge?card_number=" + cardNumber + "&amount=" + amount);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) sb.append(scanner.nextLine());
            scanner.close();

            JSONObject result = new JSONObject(sb.toString());
            boolean success = result.getBoolean("success");

            runOnUiThread(() -> {
                if (success) {
                    statusText.setText("✓ Pago completado");
                } else {
                    String reason = result.optString("reason", "Error desconocido");
                    statusText.setText("✗ Pago rechazado: " + reason);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error cargando tarjeta: " + e.getMessage());
            runOnUiThread(() -> statusText.setText("Error: " + e.getMessage()));
        }
    }).start();
    }
}