package com.zebra.waltermartmobilecollector.services;

import android.os.Build;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class Encryptor {

    //    key = newtonprogrammer
    private static byte[] key = new byte[]{110, 101, 119, 116, 111, 110, 112, 114, 111, 103, 114, 97, 109, 109, 101, 114};

    public static String encrypt(String data) throws Exception {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) throw new Exception("Cannot encrypt password in api lower than 26!!!");

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return Base64
                    .getEncoder()
                    .encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception _){
            throw new Exception("Error encrypting this password!!!");
        }
    }

}
