package com.market.dao;

import com.market.config.MongoDBConnection;
import com.market.model.User;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class UserDAO {

    private final MongoCollection<Document> collection;

    public UserDAO() {

        MongoDatabase database =
                MongoDBConnection.getDatabase();

        collection =
                database.getCollection("users");
    }

    // =====================================
    // GET ALL
    // =====================================
    public List<User> getAll() {

        List<User> users =
                new ArrayList<>();

        for (Document doc : collection.find()) {

            users.add(
                    documentToUser(doc)
            );
        }

        return users;
    }

    // =====================================
    // GET BY ID
    // =====================================
    public User getById(String id) {

        if (id == null
                || !ObjectId.isValid(id)) {

            return null;
        }

        Document doc =
                collection.find(
                        eq(
                                "_id",
                                new ObjectId(id)
                        )
                ).first();

        if (doc == null) {
            return null;
        }

        return documentToUser(doc);
    }

    // =====================================
    // FIND BY EMAIL
    // Dùng cho Login
    // =====================================
    public User findByEmail(String email) {

        if (email == null
                || email.trim().isEmpty()) {

            return null;
        }

        email =
                email.trim()
                        .toLowerCase();

        Document doc =
                collection.find(
                        eq(
                                "email",
                                email
                        )
                ).first();

        if (doc == null) {
            return null;
        }

        return documentToUser(doc);
    }

    // =====================================
    // EMAIL EXISTS
    // =====================================
    public boolean emailExists(String email) {

        return findByEmail(email) != null;
    }

    // =====================================
    // EMAIL BELONGS TO ANOTHER USER
    // =====================================
    public boolean emailExistsForOtherUser(
            String email,
            String currentUserId) {

        User user =
                findByEmail(email);

        if (user == null) {
            return false;
        }

        return !user.getId()
                .equals(currentUserId);
    }

    // =====================================
    // INSERT
    // =====================================
    public void insert(User user) {

        Document document =
                new Document()
                        .append(
                                "name",
                                user.getName()
                        )
                        .append(
                                "email",
                                user.getEmail()
                        )
                        .append(
                                "passwordHash",
                                user.getPasswordHash()
                        )
                        .append(
                                "role",
                                user.getRole()
                        );

        collection.insertOne(document);
    }

    // =====================================
    // UPDATE
    //
    // Không thay password ở đây
    // =====================================
    public boolean update(User user) {

        if (user.getId() == null
                || !ObjectId.isValid(
                        user.getId()
                )) {

            return false;
        }

        Document filter =
                new Document(
                        "_id",
                        new ObjectId(
                                user.getId()
                        )
                );

        Document newData =
                new Document()
                        .append(
                                "name",
                                user.getName()
                        )
                        .append(
                                "email",
                                user.getEmail()
                        )
                        .append(
                                "role",
                                user.getRole()
                        );

        Document update =
                new Document(
                        "$set",
                        newData
                );

        return collection
                .updateOne(
                        filter,
                        update
                )
                .getMatchedCount() > 0;
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
    // DOCUMENT -> USER
    // =====================================
    private User documentToUser(
            Document doc) {

        User user =
                new User();

        user.setId(
                doc.getObjectId("_id")
                        .toHexString()
        );

        user.setName(
                doc.getString("name")
        );

        user.setEmail(
                doc.getString("email")
        );

        user.setPasswordHash(
                doc.getString("passwordHash")
        );

        user.setRole(
                doc.getString("role")
        );

        return user;
    }
}