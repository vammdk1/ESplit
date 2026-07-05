package com.tfm.es_plit.nfc;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

public class NfcReaderHelper implements NfcAdapter.ReaderCallback {

    private static final String TAG = "NFC_READER";

    // SELECT AID — tiene que coincidir con el del servicio HCE
    private static final byte[] SELECT_AID_COMMAND = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x07,
            (byte) 0xF0, (byte) 0x39, (byte) 0x41, (byte) 0x48,
            (byte) 0x14, (byte) 0x81, (byte) 0x00
    };

    public interface NfcReadCallback {
        void onCardRead(String cardNumber);
        void onError(String message);
    }

    private final NfcReadCallback callback;

    public NfcReaderHelper(NfcReadCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            callback.onError("Tag no compatible con IsoDep");
            return;
        }

        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(SELECT_AID_COMMAND);

            if (response != null && response.length > 2) {
                byte[] data = new byte[response.length - 2];
                System.arraycopy(response, 0, data, 0, data.length);
                String cardNumber = new String(data).trim();
                Log.d(TAG, "Tarjeta leída por NFC: " + cardNumber);
                callback.onCardRead(cardNumber);
            } else {
                callback.onError("Respuesta NFC vacía o inválida");
            }

            isoDep.close();
        } catch (Exception e) {
            callback.onError("Error NFC: " + e.getMessage());
            Log.e(TAG, "Error leyendo NFC", e);
        }
    }
}