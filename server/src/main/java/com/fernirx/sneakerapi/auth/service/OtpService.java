package com.fernirx.sneakerapi.auth.service;

import com.fernirx.sneakerapi.user.enums.OtpPurpose;

public interface OtpService {
    void sendOtp(String email, String name, OtpPurpose purpose);
    void verifyOtp(String email, String rawOtp, OtpPurpose purpose);
}
