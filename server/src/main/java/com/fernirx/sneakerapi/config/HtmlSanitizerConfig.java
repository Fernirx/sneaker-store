package com.fernirx.sneakerapi.config;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cho phép đúng tập thẻ Rich Text Editor (Tiptap) tạo ra: bold/italic, list, heading, link, ảnh, bảng.
 * Bắt buộc phải sanitize server-side dù Tiptap đã giới hạn output ở client - 1 client gọi thẳng API
 * có thể gửi HTML tuỳ ý, nội dung này sẽ được render lại cho người khác qua dangerouslySetInnerHTML.
 * Dùng chung cho mọi field rich-text trong hệ thống (notification message, product/brand/category/collection description).
 */
@Configuration
public class HtmlSanitizerConfig {

    @Bean
    public PolicyFactory richTextHtmlPolicy() {
        return Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES)
                .and(Sanitizers.TABLES)
                .and(new HtmlPolicyBuilder()
                        .allowElements("h1", "h2", "h3")
                        .toFactory());
    }
}
