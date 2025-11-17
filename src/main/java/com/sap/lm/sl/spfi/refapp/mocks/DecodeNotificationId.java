package com.sap.lm.sl.spfi.refapp.mocks;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DecodeNotificationId {

    public static String decode(String notificationId, String key, String iv)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] decoded = Base64.getDecoder()
                               .decode(notificationId);

        javax.crypto.spec.SecretKeySpec keyspec = new javax.crypto.spec.SecretKeySpec(hexStringToByteArray(key), "AES");
        javax.crypto.spec.IvParameterSpec ivspec = new javax.crypto.spec.IvParameterSpec(hexStringToByteArray(iv));

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyspec, ivspec);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted).trim();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static void main(String[] args)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String notificationIdStag1 = "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/GXE8Ho7JXreziNbI3r+PM70TJNsJMvd1mpygK97EfYUzPPPU/iGrs33XBodyL61bnnRcYTthXaYWrGQkVtinW91ibxxrTFIXrSyxABwRmDVsDS52egU14tBqsPHSSjtsGWXq3lSHNu/6pQ7yjapQchGlc5nhlH3TxmTg6wUq3IYuQDuUL0pPyGo1T4ov6dgff4TGyyGU/7tju0kmDiadtoyitWgkDke54LGsZG7uEAh/cRW5pgDrRrZR4RIfFMYNjmJ8hQVujDcsODXGY7x1GPnWuL92XK5MEITb2UTEqlcV+2ifioyOXaR1pbkp2gT5Q==";
        String notificationIdStag2 = "PNRhzUFMATTF6KvZdb0QpZkyVaTDCh3YDdaOpD80Nb9RguGf2UBHoAqSt5jnexWN9NLFckOUXdy920LYeU8VQ+aVVyEJV8qq2Wb4LxZSOS4nn5auP1WuOFcyUk17DWrr6DxHn1cJVEwvgmIMUJcjfJxXU5zcqUdGgu23i+vbAvXW6axdWXSOwEjPBlC7985YIqjZTQk50fpv8/efafgEQa8dZ575BYovk908TP34PnSAPpaRYCF7Dw3skOW/1feY7uRxZ+aHAKWoPUlVghGzQU1GXk3q9+Cv0MH3KBTSJXXtkH/2wka2anzHGz3gls4uWekpOASP80ODoRJLKMGMTa9+f1oSfowZsU8l0BAFcGkfSLhpLikVveVpIlUl160TmCaMmhKy1E5TUdXEyQ8qXVhTw9Zq3e+mfREN2rKdLwPff6HRa0rD0+gXbpKXrjt7s1GWiErWTPEp8q3FOeSy/GXE8Ho7JXreziNbI3r+PM70TJNsJMvd1mpygK97EfYUzPPPU/iGrs33XBodyL61bnnRcYTthXaYWrGQkVtinW91ibxxrTFIXrSyxABwRmDVsDS52egU14tBqsPHSSjtsGWXq3lSHNu/6pQ7yjapQchGlc5nhlH3TxmTg6wUq3IYuQDuUL0pPyGo1T4ov6dgff4TGyyGU/7tju0kmDiadtoyitWgkDke54LGsZG7uEAh/cRW5pgDrRrZR4RIfFMYNjmJ8hQVujDcsODXGY7x1GMNBBn6QQuelHSe72qSAXK7a5Y17rmDbc6AsB0PBXtxqw==";

        String key = ""; // configure as secret;
        String iv = ""; // configure as secret;

        System.out.println(" notificationId1 = " + decode(notificationIdStag1, key, iv));
        System.out.println(" notificationId2 = " + decode(notificationIdStag2, key, iv));
    }
}
