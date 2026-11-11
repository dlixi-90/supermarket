package com.market.dao;

import com.market.config.MongoDBConnection;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;

import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;


public class DashboardDAO {

    private final MongoCollection<Document>
            userCollection;

    private final MongoCollection<Document>
            productCollection;

    private final MongoCollection<Document>
            orderCollection;


    public DashboardDAO() {

        MongoDatabase database =
                MongoDBConnection.getDatabase();


        userCollection =
                database.getCollection(
                        "users"
                );


        productCollection =
                database.getCollection(
                        "products"
                );


        orderCollection =
                database.getCollection(
                        "orders"
                );
    }


    // =====================================
    // TOTAL CUSTOMERS
    //
    // Chỉ đếm role customer.
    // Không đếm admin.
    // =====================================
    public long countCustomers() {

        return userCollection
                .countDocuments(
                        eq(
                                "role",
                                "customer"
                        )
                );
    }


    // =====================================
    // TOTAL PRODUCTS
    //
    // Bao gồm active + inactive.
    // Đây là số sản phẩm Admin quản lý.
    // =====================================
    public long countProducts() {

        return productCollection
                .countDocuments();
    }


    // =====================================
    // ACTIVE PRODUCTS
    // =====================================
    public long countActiveProducts() {

        return productCollection
                .countDocuments(
                        eq(
                                "active",
                                true
                        )
                );
    }


    // =====================================
    // TOTAL ORDERS
    // =====================================
    public long countOrders() {

        return orderCollection
                .countDocuments();
    }


    // =====================================
    // COUNT ORDERS BY STATUS
    // =====================================
    public long countOrdersByStatus(
            String status) {

        if (status == null
                || status.trim()
                    .isEmpty()) {

            return 0;
        }


        return orderCollection
                .countDocuments(
                        eq(
                                "status",
                                status
                        )
                );
    }


    // =====================================
    // LOW STOCK PRODUCTS
    //
    // active = true
    // stock <= threshold
    // =====================================
    public long countLowStockProducts(
            int threshold) {

        return productCollection
                .countDocuments(
                        and(
                                eq(
                                        "active",
                                        true
                                ),

                                lte(
                                        "stock",
                                        threshold
                                )
                        )
                );
    }


    // =====================================
    // DELIVERED REVENUE
    //
    // Chỉ cộng order:
    // status = Delivered
    // =====================================
    public long getDeliveredRevenue() {


        Document result =
                orderCollection
                        .aggregate(
                                Arrays.asList(

                                        new Document(
                                                "$match",
                                                new Document(
                                                        "status",
                                                        "Delivered"
                                                )
                                        ),

                                        new Document(
                                                "$group",

                                                new Document(
                                                        "_id",
                                                        null
                                                )
                                                .append(
                                                        "totalRevenue",

                                                        new Document(
                                                                "$sum",
                                                                "$totalAmount"
                                                        )
                                                )
                                        )
                                )
                        )
                        .first();


        if (result == null) {

            return 0;
        }


        Number revenue =
                result.get(
                        "totalRevenue",
                        Number.class
                );


        return revenue == null
                ? 0
                : revenue.longValue();
    }
}