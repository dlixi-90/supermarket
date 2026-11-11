package com.market.dao;

import com.market.config.MongoDBConnection;
import com.market.model.Product;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;

import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Filters.eq;

public class ProductDAO {

    private final MongoCollection<Document> collection;

    public ProductDAO() {

        MongoDatabase database
                = MongoDBConnection.getDatabase();

        collection
                = database.getCollection("products");
    }

    // =====================================
    // GET ALL
    // =====================================
    public List<Product> getAll() {

        List<Product> products
                = new ArrayList<>();

        for (Document doc
                : collection.find()) {

            products.add(
                    documentToProduct(doc)
            );
        }

        return products;
    }

    // =====================================
    // GET BY ID
    // =====================================
    public Product getById(String id) {

        if (id == null
                || !ObjectId.isValid(id)) {

            return null;
        }

        Document doc
                = collection
                        .find(
                                eq(
                                        "_id",
                                        new ObjectId(id)
                                )
                        )
                        .first();

        if (doc == null) {
            return null;
        }

        return documentToProduct(doc);
    }

    // =====================================
    // INSERT
    // =====================================
    public void insert(Product product) {

        Date now
                = new Date();

        Document document
                = new Document()
                        .append(
                                "name",
                                product.getName()
                        )
                        .append(
                                "description",
                                product.getDescription()
                        )
                        .append(
                                "price",
                                product.getPrice()
                        )
                        .append(
                                "stock",
                                product.getStock()
                        )
                        .append(
                                "categoryId",
                                new ObjectId(
                                        product.getCategoryId()
                                )
                        )
                        .append(
                                "imageUrl",
                                product.getImageUrl()
                        )
                        .append(
                                "active",
                                product.isActive()
                        )
                        .append(
                                "createdAt",
                                now
                        )
                        .append("manufactureDate", formatDate(product.getManufactureDate()))
                        .append("expiryDate", formatDate(product.getExpiryDate()))
                        .append(
                                "updatedAt",
                                now
                        );

        collection.insertOne(
                document
        );
    }

    // =====================================
    // UPDATE
    // createdAt giữ nguyên
    // updatedAt thay đổi
    // =====================================
    public boolean update(
            Product product) {

        if (product.getId() == null
                || !ObjectId.isValid(
                        product.getId()
                )) {

            return false;
        }

        if (product.getCategoryId() == null
                || !ObjectId.isValid(
                        product.getCategoryId()
                )) {

            return false;
        }

        Document filter
                = new Document(
                        "_id",
                        new ObjectId(
                                product.getId()
                        )
                );

        Document newData
                = new Document()
                        .append(
                                "name",
                                product.getName()
                        )
                        .append(
                                "description",
                                product.getDescription()
                        )
                        .append(
                                "price",
                                product.getPrice()
                        )
                        .append(
                                "stock",
                                product.getStock()
                        )
                        .append(
                                "categoryId",
                                new ObjectId(
                                        product.getCategoryId()
                                )
                        )
                        .append(
                                "imageUrl",
                                product.getImageUrl()
                        )
                        .append(
                                "active",
                                product.isActive()
                        )
                        .append(
                                "updatedAt",
                                new Date()
                        );

        Document update
                = new Document(
                        "$set",
                        newData
                                .append("manufactureDate", formatDate(product.getManufactureDate()))
                                .append("expiryDate", formatDate(product.getExpiryDate()))
                );

        return collection
                .updateOne(
                        filter,
                        update
                )
                .getMatchedCount() > 0;
    }

    // Change availability without overwriting product details or dates.
    public boolean setActive(String id, boolean active) {
        if (id == null || !ObjectId.isValid(id)) return false;
        return collection.updateOne(
                eq("_id", new ObjectId(id)),
                new Document("$set", new Document("active", active).append("updatedAt", new Date())))
                .getMatchedCount() > 0;
    }

    // Store calendar dates as ISO strings so time zones cannot shift the day.
    private static String formatDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    // =====================================
    // DELETE
    // =====================================
    public boolean delete(String id) {

        if (id == null
                || !ObjectId.isValid(id)) {

            return false;
        }

        return collection
                .deleteOne(
                        eq(
                                "_id",
                                new ObjectId(id)
                        )
                )
                .getDeletedCount() > 0;
    }

    // =====================================
    // DOCUMENT -> PRODUCT
    // =====================================
    private Product documentToProduct(
            Document doc) {

        Product product
                = new Product();

        // _id
        ObjectId id
                = doc.getObjectId("_id");

        if (id != null) {

            product.setId(
                    id.toHexString()
            );
        }

        // name
        product.setName(
                doc.getString("name")
        );

        // description
        product.setDescription(
                doc.getString("description")
        );

        // =================================
        // PRICE
        // MongoDB Number có thể là
        // Integer / Long / Double
        // =================================
        Number price
                = doc.get(
                        "price",
                        Number.class
                );

        if (price != null) {

            product.setPrice(
                    price.longValue()
            );
        }

        // stock
        Number stock
                = doc.get(
                        "stock",
                        Number.class
                );

        if (stock != null) {

            product.setStock(
                    stock.intValue()
            );
        }

        // =================================
        // CATEGORY ID
        // =================================
        Object categoryValue
                = doc.get("categoryId");

        if (categoryValue instanceof ObjectId) {

            product.setCategoryId(
                    ((ObjectId) categoryValue)
                            .toHexString()
            );

        } else if (categoryValue != null) {

            /*
             * Fallback nếu dữ liệu test cũ
             * từng lưu categoryId dạng String.
             */
            product.setCategoryId(
                    categoryValue.toString()
            );
        }

        // image
        product.setImageUrl(
                doc.getString("imageUrl")
        );

        // active
        Boolean active
                = doc.getBoolean("active");

        product.setActive(
                active != null
                        ? active
                        : false
        );

        // timestamps
        String manufactureDate = doc.getString("manufactureDate");
        String expiryDate = doc.getString("expiryDate");
        product.setManufactureDate(manufactureDate == null ? null : LocalDate.parse(manufactureDate));
        product.setExpiryDate(expiryDate == null ? null : LocalDate.parse(expiryDate));

        product.setCreatedAt(
                doc.getDate("createdAt")
        );

        product.setUpdatedAt(
                doc.getDate("updatedAt")
        );

        return product;
    }

    // =====================================
// GET ACTIVE PRODUCTS FOR SHOP
// =====================================
    public List<Product> getActiveProducts() {

        List<Product> products
                = new ArrayList<>();

        for (Document doc
                : collection
                        .find(
                                eq(
                                        "active",
                                        true
                                )
                        )
                        .sort(
                                descending(
                                        "createdAt"
                                )
                        )) {

            products.add(
                    documentToProduct(doc)
            );
        }

        return products;
    }
}
