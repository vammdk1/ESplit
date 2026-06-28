package com.tfm.es_plit.nfc;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.tfm.es_plit.data.SessionManager;

public class UserIdHceService extends HostApduService {

    //NFC del lado invitado
    private static final String TAG = "HCE";

    // SELECT AID command que mandará el host
    private static final byte[] SELECT_AID = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x07,
            (byte) 0xF0, (byte) 0x39, (byte) 0x41, (byte) 0x48,
            (byte) 0x14, (byte) 0x81, (byte) 0x00
    };

    private static final byte[] SELECT_OK = {(byte) 0x90, (byte) 0x00};
    private static final byte[] UNKNOWN_CMD = {(byte) 0x00, (byte) 0x00};

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (commandApdu == null) return UNKNOWN_CMD;

        if (isSelectAid(commandApdu)) {
            // El host seleccionó nuestra app NFC — respondemos con el user_id
            SessionManager session = new SessionManager(this);
            int userId = session.getUserId();
            String response = String.valueOf(userId);
            Log.d(TAG, "HCE: enviando user_id " + userId);

            byte[] responseBytes = response.getBytes();
            byte[] result = new byte[responseBytes.length + 2];
            System.arraycopy(responseBytes, 0, result, 0, responseBytes.length);
            result[responseBytes.length] = (byte) 0x90;
            result[responseBytes.length + 1] = (byte) 0x00;
            return result;
        }

        return UNKNOWN_CMD;
    }

    private boolean isSelectAid(byte[] apdu) {
        if (apdu.length < SELECT_AID.length) return false;
        for (int i = 0; i < SELECT_AID.length; i++) {
            if (apdu[i] != SELECT_AID[i]) return false;
        }
        return true;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "HCE desactivado, razón: " + reason);
    }
}