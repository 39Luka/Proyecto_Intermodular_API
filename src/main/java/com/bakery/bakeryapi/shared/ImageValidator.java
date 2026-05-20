package com.bakery.bakeryapi.shared;

import com.bakery.bakeryapi.shared.exception.InvalidImageException;

/**
 * Valida imágenes codificadas en Base64 aceptadas por la API.
 */
public class ImageValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg",
            "image/png",
            "image/webp"
    };

    /**
     * Valida una carga de imagen codificada en Base64.
     *
     * Los valores vacíos se aceptan para que los llamadores puedan usarlos para significar "sin imagen".
     *
     * @param imageBase64 imagen codificada como Base64
     * @throws InvalidImageException cuando el contenido no es Base64 válido, demasiado grande o no compatible
     */
    public static void validateImageBase64(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return;
        }

        byte[] decodedBytes;
        try {
            decodedBytes = java.util.Base64.getDecoder().decode(imageBase64);
        } catch (IllegalArgumentException e) {
            throw new InvalidImageException("Codificación base64 inválida");
        }

        if (decodedBytes.length > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidImageException(
                    "La imagen excede el tamaño máximo de 5MB (recibido "
                            + formatBytes(decodedBytes.length)
                            + ")"
            );
        }

        String detectedMimeType = detectMimeType(decodedBytes);
        if (!isAllowedMimeType(detectedMimeType)) {
            throw new InvalidImageException(
                    "Tipo de imagen no soportado. Permitidos: JPEG, PNG, WebP (detectado: "
                            + detectedMimeType
                            + ")"
            );
        }
    }

    private static String detectMimeType(byte[] bytes) {
        if (bytes.length < 4) {
            return "desconocido";
        }

        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8
                && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        if (bytes[0] == 0x52 && bytes[1] == 0x49
                && bytes[2] == 0x46 && bytes[3] == 0x46
                && bytes.length >= 12
                && bytes[8] == 0x57 && bytes[9] == 0x45
                && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return "image/webp";
        }

        return "desconocido";
    }

    private static boolean isAllowedMimeType(String mimeType) {
        for (String allowed : ALLOWED_MIME_TYPES) {
            if (allowed.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
