package com.market.model;

public class OrderItem {

    private String productId;
    private String productName;
    private String imageUrl;

    private long price;
    private int quantity;
    private long subtotal;


    public OrderItem() {
    }


    public OrderItem(
            String productId,
            String productName,
            long price,
            int quantity) {

        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;

        this.subtotal =
                price * quantity;
    }


    public OrderItem(
            String productId,
            String productName,
            long price,
            int quantity,
            long subtotal) {

        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
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

    public void setImageUrl(String imageUrl) {
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


    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            long subtotal) {

        this.subtotal = subtotal;
    }
}
