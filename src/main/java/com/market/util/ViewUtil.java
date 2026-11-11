package com.market.util;

import java.util.Locale;

public class ViewUtil {

    private ViewUtil() {
    }

    public static String formatCurrency(long value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,d ₫", value);
    }


    // =====================================
    // HTML ESCAPE
    // =====================================
    public static String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String resolveImageUrl(
            String imageUrl,
            String contextPath) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return "";
        }

        String value = imageUrl.trim();
        if (value.startsWith("https://") || value.startsWith("http://")) {
            return value;
        }

        String relativePath = value.replaceFirst("^/+", "");
        if (relativePath.isBlank()) {
            return "";
        }

        String context = contextPath == null ? "" : contextPath.trim();
        return context + "/" + relativePath;
    }


    // =====================================
    // ORDER STATUS CSS
    // =====================================
    public static String orderStatusClass(
            String status) {

        if ("Pending".equals(status)) {
            return "badge-pending";
        }

        if ("Processing".equals(status)) {
            return "badge-processing";
        }

        if ("Shipped".equals(status)) {
            return "badge-shipped";
        }

        if ("Delivered".equals(status)) {
            return "badge-delivered";
        }

        if ("Cancelled".equals(status)) {
            return "badge-cancelled";
        }

        return "";
    }
}
