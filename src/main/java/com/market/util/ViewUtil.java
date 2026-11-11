package com.market.util;

public class ViewUtil {

    private ViewUtil() {
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