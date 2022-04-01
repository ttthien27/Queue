package com.android.queue.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Encode {
    static  final  String MD5 = "MD5";

    public static String endCode(String text){
        try {
            MessageDigest digest  = MessageDigest.getInstance(MD5);
            digest.update(text.getBytes());
            byte messageDisgest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMsgDigest : messageDisgest){
                String h = Integer.toHexString(0xFF & aMsgDigest);

                while (h.length() < 2)
                    h = "0" +h;
                hexString.append(h);
            }
            return hexString.toString();

        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
