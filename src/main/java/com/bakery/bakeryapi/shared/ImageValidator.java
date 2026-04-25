package com.bakery.bakeryapi.shared;

import com.bakery.bakeryapi.shared.exception.InvalidImageException;

public class ImageValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg",
            "image/png",
            "image/webp"
    };

    public static void validateImageBase64(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return;
        }

        byte[] decodedBytes;
        try {
            decodedBytes = java.util.Base64.getDecoder().decode(imageBase64);
        } catch (IllegalArgumentException e) {
            throw new InvalidImageException("Invalid base64 encoding");
        }

        if (decodedBytes.length > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidImageException(
                    "Image exceeds maximum size of 5MB (got "
                            + formatBytes(decodedBytes.length)
                            + ")"
            );
        }

        String detectedMimeType = detectMimeType(decodedBytes);
        if (!isAllowedMimeType(detectedMimeType)) {
            throw new InvalidImageException(
                    "Image type not supported. Allowed: JPEG, PNG, WebP (detected: "
                            + detectedMimeType
                            + ")"
            );
        }
    }

    private static String detectMimeType(byte[] bytes) {
        if (bytes.length < 4) {
            return "unknown";
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

        return "unknown";
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
