package com.fernirx.sneakerapi.common.constant;

public final class PatternConstants {
    private PatternConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }

    public static final String PHONE = "^\\+[1-9]\\d{8,14}$";
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
    public static final String OTP = "^\\d{6}$";
}
