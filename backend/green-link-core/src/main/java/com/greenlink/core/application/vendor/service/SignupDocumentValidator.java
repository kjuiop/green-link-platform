package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class SignupDocumentValidator {

    static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "pdf");

    public void validate(SignupDocument doc) {
        if (doc == null || doc.content() == null || doc.content().length == 0) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_REQUIRED);
        }
        if (doc.content().length > MAX_BYTES) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_TOO_LARGE);
        }
        if (!EXTENSIONS.contains(extensionOf(doc.fileName())) || !hasKnownSignature(doc.content())) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_INVALID_TYPE);
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean hasKnownSignature(byte[] b) {
        return startsWith(b, 0, 0xFF, 0xD8, 0xFF)                       // JPEG
            || startsWith(b, 0, 0x89, 0x50, 0x4E, 0x47)                 // PNG
            || startsWith(b, 0, 0x47, 0x49, 0x46, 0x38)                 // GIF
            || startsWith(b, 0, 0x25, 0x50, 0x44, 0x46)                 // PDF
            || (startsWith(b, 0, 0x52, 0x49, 0x46, 0x46) && startsWith(b, 8, 0x57, 0x45, 0x42, 0x50)) // WEBP
            || startsWith(b, 4, 0x66, 0x74, 0x79, 0x70);                // HEIC/HEIF (ftyp)
    }

    private boolean startsWith(byte[] b, int offset, int... expected) {
        if (b.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if ((b[offset + i] & 0xFF) != expected[i]) return false;
        }
        return true;
    }
}
