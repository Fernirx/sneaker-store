package com.fernirx.sneakerapi.payment.provider.vnpay;

import com.fernirx.sneakerapi.payment.config.VNPayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class VNPaySignature {
    private static final String HMAC_SHA512 = "HmacSHA512";

    private final VNPayProperties properties;

    public String sign(Map<String, String> params) {
        String data = buildHashData(params);
        return hmacSha512(properties.getHashSecret(), data);
    }

    public boolean verify(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null) return false;

        Map<String, String> filtered = new TreeMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");

        return received.equalsIgnoreCase(sign(filtered));
    }

    /** Sắp xếp param theo thứ tự alphabet rồi nối thành key=value&... */
    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(params).forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(k).append('=');
                sb.append(URLEncoder.encode(v, StandardCharsets.US_ASCII));
            }
        });
        return sb.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi ký VNPay HMAC-SHA512", e);
        }
    }
}