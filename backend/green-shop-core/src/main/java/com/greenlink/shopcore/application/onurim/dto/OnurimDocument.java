package com.greenlink.shopcore.application.onurim.dto;

public record OnurimDocument(String fileName, String contentType, byte[] content) {

    @Override
    public String toString() {
        return "OnurimDocument[fileName=" + fileName + ", size=" + (content == null ? 0 : content.length) + "]";
    }
}
