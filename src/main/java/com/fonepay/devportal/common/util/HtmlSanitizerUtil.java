package com.fonepay.devportal.common.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlSanitizerUtil {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowCommonBlockElements() // p, div, h1-h6, ul, ol, li, blockquote, etc
            .allowCommonInlineFormattingElements() // b, i, em, strong, a, etc
            .allowStandardUrlProtocols()
            .allowElements("table", "tr", "td", "th", "tbody", "thead", "tfoot", "br", "hr", "span", "pre", "code")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .allowAttributes("alt").onElements("img")
            .allowElements("img")
            .allowUrlProtocols("http", "https", "mailto")
            .toFactory();

    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return POLICY.sanitize(html);
    }
}
