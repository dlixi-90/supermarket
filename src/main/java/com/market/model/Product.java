package com.market.model;

import java.util.Date;
import java.time.LocalDate;

public class Product {

    private String id;
    private String name;
    private String description;
    private long price;
    private int stock;

    private String categoryId;

    /*
     * Chỉ dùng để hiển thị.
     * Không lưu vào MongoDB.
     */
    private String categoryName;

    private String imageUrl;
    private boolean active;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    private Date createdAt;
    private Date updatedAt;


    public Product() {
    }


    // =====================================
    // FULL CONSTRUCTOR
    // =====================================
    public Product(
            String id,
            String name,
            String description,
            long price,
            int stock,
            String categoryId,
            String categoryName,
            String imageUrl,
            boolean active,
            Date createdAt,
            Date updatedAt) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // =====================================
    // INSERT CONSTRUCTOR
    // createdAt / updatedAt do DAO tạo
    // =====================================
    public Product(
            String name,
            String description,
            long price,
            int stock,
            String categoryId,
            String imageUrl,
            boolean active) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.active = active;
    }


    // =====================================
    // UPDATE CONSTRUCTOR
    // =====================================
    public Product(
            String id,
            String name,
            String description,
            long price,
            int stock,
            String categoryId,
            String imageUrl,
            boolean active) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.active = active;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }


    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }


    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(
            String categoryId) {

        this.categoryId = categoryId;
    }


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(
            String categoryName) {

        this.categoryName = categoryName;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(
            String imageUrl) {

        this.imageUrl = imageUrl;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active) {

        this.active = active;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDate manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setCreatedAt(
            Date createdAt) {

        this.createdAt = createdAt;
    }


    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            Date updatedAt) {

        this.updatedAt = updatedAt;
    }
}
