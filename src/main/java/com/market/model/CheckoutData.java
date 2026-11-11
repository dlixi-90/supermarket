package com.market.model;

import java.io.Serializable;

public class CheckoutData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String recipientName;
    private String phone;
    private String shippingAddress;
    private String paymentMethod;


    public CheckoutData() {
    }


    public CheckoutData(
            String recipientName,
            String phone,
            String shippingAddress,
            String paymentMethod) {

        this.recipientName = recipientName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }


    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(
            String recipientName) {

        this.recipientName = recipientName;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(
            String phone) {

        this.phone = phone;
    }


    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(
            String shippingAddress) {

        this.shippingAddress =
                shippingAddress;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            String paymentMethod) {

        this.paymentMethod =
                paymentMethod;
    }
}