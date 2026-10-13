package com.instituto.tardanzas.config;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cifrado AES-GCM para guardar la contraseña SMTP en la base de datos
 * y la contraseña de MySQL en config.properties.
 * La clave se genera la primera vez y se guarda en
 * %APPDATA%\ControlTardanzas\smtp.key (fuera del repositorio).
 * Formato: Base64( IV(12) + cifrado )
 */
public final class CryptoUtil {

    private static final String ALGORITMO    = "AES/GCM/NoPadding";
    private static final int    TAMANO_CLAVE = 16;   // 128 bits
    private static final int    TAMANO_IV    = 12;   // GCM estándar
    private static final int    TAMANO_TAG   = 128;  // bits

    private static volatile SecretKeySpec clave = null;

    private CryptoUtil() {}

    public static String cifrar(String textoPlano) throws Exception {
        if (textoPlano == null || textoPlano.isEmpty()) return "";
        SecretKeySpec k = obtenerClave();
        byte[] iv = new byte[TAMANO_IV];
        new SecureRandom().nextBytes(iv);
        Cipher c = Cipher.getInstance(ALGORITMO);
        c.init(Cipher.ENCRYPT_MODE, k, new GCMParameterSpec(TAMANO_TAG, iv));
        byte[] cifrado = c.doFinal(
                textoPlano.getBytes(StandardCharsets.UTF_8));
        byte[] completo = new byte[TAMANO_IV + cifrado.length];
        System.arraycopy(iv, 0, completo, 0, TAMANO_IV);
        System.arraycopy(cifrado, 0, completo, TAMANO_IV, cifrado.length);
        return Base64.getEncoder().encodeToString(completo);
    }

    public static String descifrar(String textoCifrado) throws Exception {
        if (textoCifrado == null || textoCifrado.isEmpty()) return "";
        SecretKeySpec k = obtenerClave();
        byte[] completo = Base64.getDecoder().decode(textoCifrado);
        if (completo.length < TAMANO_IV + 1)
            throw new Exception("Datos cifrados no validos");
        byte[] iv      = Arrays.copyOfRange(completo, 0, TAMANO_IV);
        byte[] cifrado = Arrays.copyOfRange(
                completo, TAMANO_IV, completo.length);
        Cipher c = Cipher.getInstance(ALGORITMO);
        c.init(Cipher.DECRYPT_MODE, k, new GCMParameterSpec(TAMANO_TAG, iv));
        byte[] plano = c.doFinal(cifrado);
        return new String(plano, StandardCharsets.UTF_8);
    }

    private static synchronized SecretKeySpec obtenerClave() throws Exception {
        if (clave != null) return clave;
        File archivo = archivoClave();
        byte[] bytes;
        if (archivo.exists() && archivo.length() > 0) {
            bytes = Files.readAllBytes(archivo.toPath());
            if (bytes.length != TAMANO_CLAVE) {
                throw new Exception(
                        "Archivo de clave no valido: "
                        + archivo.getAbsolutePath());
            }
        } else {
            bytes = new byte[TAMANO_CLAVE];
            new SecureRandom().nextBytes(bytes);
            File dir = archivo.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            Files.write(archivo.toPath(), bytes);
        }
        clave = new SecretKeySpec(bytes, "AES");
        return clave;
    }

    private static File archivoClave() {
        String dir = System.getenv("APPDATA") != null
                ? System.getenv("APPDATA")
                    + File.separator + "ControlTardanzas"
                : System.getProperty("user.home")
                    + File.separator + ".controltardanzas";
        return new File(dir, "smtp.key");
    }
}
