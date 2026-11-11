package com.market.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {

    private String id;

    private String userId;

    private String recipientName;
    private String phone;
    private String shippingAddress;

    private Date orderDate;

    private List<OrderItem> items;

    private long totalAmount;

    private String paymentMethod;
    private String status;


    public Order() {

        items =
                new ArrayList<>();
    }


    public Order(
            String id,
            String userId,
            String recipientName,
            String phone,
            String shippingAddress,
            Date orderDate,
            List<OrderItem> items,
            long totalAmount,
            String paymentMethod,
            String status) {

        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.items = items;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }


    public Order(
            String userId,
            String recipientName,
            String phone,
            String shippingAddress,
            Date orderDate,
            List<OrderItem> items,
            long totalAmount,
            String paymentMethod,
            String status) {

        this.userId = userId;
        this.recipientName = recipientName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.items = items;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(
            String userId) {

        this.userId = userId;
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


    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(
            Date orderDate) {

        this.orderDate = orderDate;
    }


    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(
            List<OrderItem> items) {

        this.items = items;
    }


    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            long totalAmount) {

        this.totalAmount = totalAmount;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            String paymentMethod) {

        this.paymentMethod =
                paymentMethod;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }
}