package com.wooteco.wiki.util;

import com.wooteco.wiki.exception.WikiException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Encryptor {
    public static String encrypt(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new WikiException("암호화 과정에 문제가 발생했습니다.");
        }
    }
}
