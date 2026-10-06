package com.tontine.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Empreinte SHA-256 d'un fichier (piece d'identite) : sert a detecter les doublons frauduleux. */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Fichier(File fichier) throws IOException {
        try (InputStream in = Files.newInputStream(fichier.toPath())) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                md.update(buffer, 0, n);
            }
            return hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String hex(byte[] octets) {
        StringBuilder sb = new StringBuilder();
        for (byte b : octets) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
