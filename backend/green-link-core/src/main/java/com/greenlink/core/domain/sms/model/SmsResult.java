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
        if (message != null && !message.isBlank()) return message;
        String name = e.getClass().getSimpleName();
        String location = locationOf(e);
        return location == null ? name : name + " (" + location + ")";
    }

    public static String rejectionReason(String code, Object message) {
        String m = message == null ? null : String.valueOf(message);
        if (m == null || m.isBlank()) return code;
        return code + ": " + m;
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
