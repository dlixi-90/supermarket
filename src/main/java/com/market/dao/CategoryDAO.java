package com.market.dao;

import com.market.config.MongoDBConnection;
import com.market.model.Category;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class CategoryDAO {

    private final MongoCollection<Document> collection;

    public CategoryDAO() {

        MongoDatabase database =
                MongoDBConnection.getDatabase();

        collection =
                database.getCollection("categories");
    }

    // =========================
    // GET ALL
    // =========================
    public List<Category> getAll() {

        List<Category> categories =
                new ArrayList<>();

        for (Document doc : collection.find()) {

            Category category = new Category();

            category.setId(
                    doc.getObjectId("_id").toHexString()
            );

            category.setName(
                    doc.getString("name")
            );

            category.setDescription(
                    doc.getString("description")
            );

            category.setImageUrl(doc.getString("imageUrl"));
            categories.add(category);
        }

        return categories;
    }

    // =========================
    // GET BY ID
    // =========================
    public Category getById(String id) {

        if (id == null || !ObjectId.isValid(id)) {
            return null;
        }

        Document doc = collection.find(
                eq("_id", new ObjectId(id))
        ).first();

        if (doc == null) {
            return null;
        }

        Category category = new Category(
                doc.getObjectId("_id").toHexString(),
                doc.getString("name"),
                doc.getString("description")
        );
        category.setImageUrl(doc.getString("imageUrl"));
        return category;
    }

    // =========================
    // INSERT
    // =========================
    public void insert(Category category) {

        Document document =
                new Document()
                        .append(
                                "name",
                                category.getName()
                        )
                        .append(
                                "description",
                                category.getDescription()
                        )
                        .append("imageUrl", category.getImageUrl());

        collection.insertOne(document);
    }

    // =========================
    // UPDATE
    // =========================
    public boolean update(Category category) {

        if (!ObjectId.isValid(category.getId())) {
            return false;
        }

        Document filter =
                new Document(
                        "_id",
                        new ObjectId(category.getId())
                );

        Document newData =
                new Document()
                        .append(
                                "name",
                                category.getName()
                        )
                        .append(
                                "description",
                                category.getDescription()
                        )
                        .append("imageUrl", category.getImageUrl());

        Document update =
                new Document(
                        "$set",
                        newData
                );

        return collection
                .updateOne(filter, update)
                .getMatchedCount() > 0;
    }

    // =========================
    // DELETE
    // =========================
    public boolean delete(String id) {

        if (!ObjectId.isValid(id)) {
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
}