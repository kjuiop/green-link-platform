package com.greenlink.core.domain.sms.model;

public record SmsResult(boolean success, String msgId, String failReason, boolean rejectedByProvider) {

    public SmsResult(boolean success, String msgId, String failReason) {
        this(success, msgId, failReason, false);
    }

    public static SmsResult rejected(String failReason) {
        return new SmsResult(false, null, failReason, true);
    }

    public static String reasonOf(Throwable e) {
        String message = e.getMessage();
        String base = (message == null || message.isBlank()) ? e.getClass().getSimpleName() : message;
        String location = locationOf(e);
        return location == null ? base : base + " (" + location + ")";
    }

    private static String locationOf(Throwable e) {
        for (StackTraceElement frame : e.getStackTrace()) {
            if (frame.getClassName().startsWith("com.greenlink.")) {
                String className = frame.getClassName();
                String simple = className.substring(className.lastIndexOf('.') + 1);
                return simple + "." + frame.getMethodName() + ":" + frame.getLineNumber();
            }
        }
        return null;
    }
}
