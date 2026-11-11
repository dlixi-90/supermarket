package com.market.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class PasswordUtil {

    private static final int ITERATIONS = 100000;

    private static final int KEY_LENGTH = 256;

    private static final int SALT_LENGTH = 16;


    // =====================================
    // HASH PASSWORD
    // =====================================
    public static String hashPassword(
            String password) {

        try {

            SecureRandom random =
                    new SecureRandom();

            byte[] salt =
                    new byte[SALT_LENGTH];

            random.nextBytes(salt);


            byte[] hash =
                    pbkdf2(
                            password.toCharArray(),
                            salt,
                            ITERATIONS,
                            KEY_LENGTH
                    );


            String saltBase64 =
                    Base64.getEncoder()
                            .encodeToString(salt);

            String hashBase64 =
                    Base64.getEncoder()
                            .encodeToString(hash);


            /*
             * Lưu dạng:
             *
             * iterations:salt:hash
             */
            return ITERATIONS
                    + ":"
                    + saltBase64
                    + ":"
                    + hashBase64;


        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not hash password",
                    e
            );
        }
    }


    // =====================================
    // VERIFY PASSWORD
    //
    // LoginServlet sau này sẽ dùng method này
    // =====================================
    public static boolean verifyPassword(
            String password,
            String storedPasswordHash) {

        try {

            if (password == null
                    || storedPasswordHash == null
                    || storedPasswordHash.isEmpty()) {

                return false;
            }


            String[] parts =
                    storedPasswordHash.split(":");

            if (parts.length != 3) {
                return false;
            }


            int iterations =
                    Integer.parseInt(
                            parts[0]
                    );


            byte[] salt =
                    Base64
                            .getDecoder()
                            .decode(
                                    parts[1]
                            );


            byte[] storedHash =
                    Base64
                            .getDecoder()
                            .decode(
                                    parts[2]
                            );


            byte[] calculatedHash =
                    pbkdf2(
                            password.toCharArray(),
                            salt,
                            iterations,
                            storedHash.length * 8
                    );


            return slowEquals(
                    storedHash,
                    calculatedHash
            );


        } catch (Exception e) {

            return false;
        }
    }


    // =====================================
    // PBKDF2
    // =====================================
    private static byte[] pbkdf2(
            char[] password,
            byte[] salt,
            int iterations,
            int keyLength)
            throws Exception {

        PBEKeySpec spec =
                new PBEKeySpec(
                        password,
                        salt,
                        iterations,
                        keyLength
                );


        SecretKeyFactory factory;

        try {

            factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

        } catch (NoSuchAlgorithmException e) {

            /*
             * Fallback cho Java cũ hơn.
             */
            factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA1"
                    );
        }


        try {

            return factory
                    .generateSecret(spec)
                    .getEncoded();

        } finally {

            spec.clearPassword();
        }
    }


    // =====================================
    // CONSTANT-TIME COMPARISON
    // =====================================
    private static boolean slowEquals(
            byte[] a,
            byte[] b) {

        if (a == null || b == null) {
            return false;
        }


        int diff =
                a.length ^ b.length;


        int length =
                Math.min(
                        a.length,
                        b.length
                );


        for (int i = 0;
                i < length;
                i++) {

            diff |=
                    a[i] ^ b[i];
        }


        return diff == 0;
    }
}