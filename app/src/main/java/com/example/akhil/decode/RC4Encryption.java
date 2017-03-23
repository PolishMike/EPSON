package com.example.akhil.decode;

import android.util.Log;

/**
 * Created by akhil on 11/3/17.
 */

class RC4Encryption {

    InitRC4 initRC4;
    private int[] prga;

    public RC4Encryption(InitRC4 initRC4) {
        this.initRC4 = initRC4;
    }

    public String encryptCode(String buttonCode ) {

        StringBuilder encryptedCode = new StringBuilder();
        encryptedCode.setLength(buttonCode.length());
        prga = initRC4.getPrga();

        //Log.d("PRGA",prga.toString());

        for(int i = 0; i<buttonCode.length(); i++) {
            encryptedCode.setCharAt(i, (char) (prga[i] ^ buttonCode.charAt(i)));
            //Log.d("PRGA",String.valueOf(prga[i]));
        }
        Log.d("RC4Encryption",encryptedCode.toString());


        return encryptedCode.toString();

    }

}
