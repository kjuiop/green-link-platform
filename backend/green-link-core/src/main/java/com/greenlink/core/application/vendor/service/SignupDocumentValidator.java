package com.greenlink.core.application.vendor.service;

import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.vendor.VendorErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Component
public class SignupDocumentValidator {

    static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "pdf");

    private static final Set<String> HEIC_BRANDS = Set.of("heic", "heix", "heim", "heis", "hevc", "mif1", "msf1");

    public SignupDocument validate(SignupDocument doc) {
        if (doc == null || doc.content() == null || doc.content().length == 0) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_REQUIRED);
        }
        if (doc.content().length > MAX_BYTES) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_TOO_LARGE);
        }
        String contentType = detectContentType(doc.content());
        if (!EXTENSIONS.contains(extensionOf(doc.fileName())) || contentType == null) {
            throw new BusinessException(VendorErrorCode.DOCUMENT_INVALID_TYPE);
        }
        return new SignupDocument(doc.fileName(), doc.content(), contentType);
    }

    private String extensionOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String detectContentType(byte[] b) {
        if (startsWith(b, 0, 0xFF, 0xD8, 0xFF)) return "image/jpeg";
        if (startsWith(b, 0, 0x89, 0x50, 0x4E, 0x47)) return "image/png";
        if (startsWith(b, 0, 0x47, 0x49, 0x46, 0x38)) return "image/gif";
        if (startsWith(b, 0, 0x25, 0x50, 0x44, 0x46)) return "application/pdf";
        if (startsWith(b, 0, 0x52, 0x49, 0x46, 0x46) && startsWith(b, 8, 0x57, 0x45, 0x42, 0x50)) return "image/webp";
        if (startsWith(b, 4, 0x66, 0x74, 0x79, 0x70) && isHeicBrand(b)) return "image/heic";
        return null;
    }

    private boolean isHeicBrand(byte[] b) {
        if (b.length < 12) return false;
        String brand = new String(b, 8, 4, StandardCharsets.US_ASCII);
        return HEIC_BRANDS.contains(brand);
    }

    private boolean startsWith(byte[] b, int offset, int... expected) {
        if (b.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if ((b[offset + i] & 0xFF) != expected[i]) return false;
        }
        return true;
    }
}
