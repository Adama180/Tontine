package com.tontine.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Hachage des mots de passe : SHA-256 avec sel aleatoire, 10 000 tours. Format stocke : sel:hash (Base64). */
public final class PasswordUtil {

    private static final int TOURS = 10_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hasher(String motDePasse) {
        byte[] sel = new byte[16];
        RANDOM.nextBytes(sel);
        byte[] hash = calculer(motDePasse, sel);
        return Base64.getEncoder().encodeToString(sel) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifier(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null || !stocke.contains(":")) {
            return false;
        }
        String[] parts = stocke.split(":", 2);
        byte[] sel = Base64.getDecoder().decode(parts[0]);
        byte[] attendu = Base64.getDecoder().decode(parts[1]);
        return MessageDigest.isEqual(attendu, calculer(motDePasse, sel));
    }

    private static byte[] calculer(String motDePasse, byte[] sel) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(sel);
            byte[] h = md.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            for (int i = 1; i < TOURS; i++) {
                h = md.digest(h);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
