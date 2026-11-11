package com.market.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String imageUrl;

    private long price;
    private int quantity;
    private int stock;


    public CartItem() {
    }


    public CartItem(
            String productId,
            String productName,
            String imageUrl,
            long price,
            int quantity,
            int stock) {

        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.stock = stock;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(
            String productId) {

        this.productId = productId;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName) {

        this.productName = productName;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(
            String imageUrl) {

        this.imageUrl = imageUrl;
    }


    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(
            int quantity) {

        this.quantity = quantity;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }


    // =====================================
    // SUBTOTAL
    // =====================================
    public long getSubtotal() {

        return price * quantity;
    }
}