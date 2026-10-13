package com.instituto.tardanzas.config;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades para guardar contraseñas de usuario con hash (PBKDF2).
 * Sin dependencias externas: usa solo clases del JDK.
 * Formato almacenado: pbkdf2:<iteraciones>:<salt_base64>:<hash_base64>
 */
public final class PasswordUtil {

    private static final String ALGORITMO   = "PBKDF2WithHmacSHA256";
    private static final int    ITERACIONES = 120_000;
    private static final int    TAMANO_SAL  = 16;   // bytes
    private static final int    TAMANO_HASH = 32;   // bytes
    private static final String PREFIJO     = "pbkdf2:";

    private PasswordUtil() {}

    /** Genera el hash de una contraseña (con salt aleatorio). */
    public static String hash(String password) throws Exception {
        byte[] salt = new byte[TAMANO_SAL];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivar(password.toCharArray(), salt, ITERACIONES);
        return PREFIJO + ITERACIONES + ":"
             + Base64.getEncoder().encodeToString(salt) + ":"
             + Base64.getEncoder().encodeToString(hash);
    }

    /** Comprueba una contraseña contra un valor almacenado. */
    public static boolean verificar(String password, String almacenado) {
        if (password == null || almacenado == null) return false;
        try {
            String[] partes = almacenado.split(":");
            if (!almacenado.startsWith(PREFIJO) || partes.length != 4)
                return false;
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt     = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(password.toCharArray(), salt, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (Exception e) {
            return false;
        }
    }

    /** True si el valor almacenado ya tiene formato de hash nuevo. */
    public static boolean esHash(String almacenado) {
        return almacenado != null && almacenado.startsWith(PREFIJO);
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones)
            throws Exception {
        PBEKeySpec spec =
                new PBEKeySpec(password, salt, iteraciones, TAMANO_HASH * 8);
        try {
            SecretKeyFactory f =
                    SecretKeyFactory.getInstance(ALGORITMO);
            return f.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
