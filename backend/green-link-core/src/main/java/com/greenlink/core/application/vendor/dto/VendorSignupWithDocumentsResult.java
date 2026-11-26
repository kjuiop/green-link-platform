package com.greenlink.core.application.vendor.dto;

import java.util.List;

public record VendorSignupWithDocumentsResult(VendorResult vendor, List<String> failedDocuments) {}
