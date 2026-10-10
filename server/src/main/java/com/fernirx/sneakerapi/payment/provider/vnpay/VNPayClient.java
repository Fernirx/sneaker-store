package com.fernirx.sneakerapi.payment.provider.vnpay;

import com.fernirx.sneakerapi.payment.config.VNPayProperties;
import com.fernirx.sneakerapi.payment.dto.request.BuildPaymentUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class VNPayClient {
    private static final DateTimeFormatter VNPAY_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String VERSION   = "2.1.0";
    private static final String COMMAND   = "pay";
    private static final String CURR_CODE = "VND";
    private static final String LOCALE    = "vn";

    private final VNPayProperties properties;

    public Map<String, String> buildBaseParams(BuildPaymentUrlRequest request, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        String txnRef = request.orderId() + "_" + now.format(VNPAY_DATE);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    VERSION);
        params.put("vnp_Command",    COMMAND);
        params.put("vnp_TmnCode",    properties.getTmnCode());
        params.put("vnp_Amount",     request.amount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode",   CURR_CODE);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo",  "Thanh toan " + request.orderCode());
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     LOCALE);
        params.put("vnp_ReturnUrl",  properties.getReturnUrl());
        params.put("vnp_IpAddr",     ipAddress);
        params.put("vnp_CreateDate", now.format(VNPAY_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(properties.getExpireMinutes()).format(VNPAY_DATE));
        return params;
    }

    public String buildUrl(Map<String, String> params, String hash) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((k, v) ->
            joiner.add(URLEncoder.encode(k, StandardCharsets.US_ASCII)
                + "=" + URLEncoder.encode(v, StandardCharsets.US_ASCII))
        );
        joiner.add("vnp_SecureHash=" + URLEncoder.encode(hash, StandardCharsets.US_ASCII));
        return properties.getPaymentUrl() + "?" + joiner;
    }
}