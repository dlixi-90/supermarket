package com.market.dao;

import com.market.config.MongoDBConnection;

import com.market.model.CartItem;
import com.market.model.CheckoutData;
import com.market.model.Order;
import com.market.model.OrderItem;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;

import static com.mongodb.client.model.Sorts.descending;

import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

public class OrderDAO {

    private final MongoCollection<Document> orderCollection;

    private final MongoCollection<Document> productCollection;

    public OrderDAO() {

        MongoDatabase database
                = MongoDBConnection.getDatabase();

        orderCollection
                = database.getCollection(
                        "orders"
                );

        productCollection
                = database.getCollection(
                        "products"
                );
    }

    // =====================================
    // PLACE ORDER
    //
    // Transaction:
    //
    // check stock
    // decrease stock
    // insert order
    //
    // Nếu bất kỳ bước nào lỗi:
    // rollback toàn bộ.
    // =====================================
    public String placeOrder(
            String userId,
            CheckoutData checkoutData,
            Map<String, CartItem> cart) {

        // =================================
        // BASIC VALIDATION
        // =================================
        if (userId == null
                || !ObjectId.isValid(
                        userId
                )) {

            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        if (checkoutData == null) {

            throw new IllegalArgumentException(
                    "Thiếu thông tin thanh toán."
            );
        }

        if (cart == null
                || cart.isEmpty()) {

            throw new IllegalArgumentException(
                    "Giỏ hàng đang trống."
            );
        }

        validateCheckoutData(
                checkoutData
        );


        /*
         * Atlas hỗ trợ transaction.
         *
         * withTransaction sẽ commit nếu
         * callback hoàn tất thành công.
         *
         * Nếu exception xảy ra:
         * transaction sẽ abort.
         */
        try (
                ClientSession clientSession
                = MongoDBConnection
                        .getClient()
                        .startSession()) {

                    return clientSession
                            .withTransaction(
                                    () -> createOrderTransaction(
                                            clientSession,
                                            userId,
                                            checkoutData,
                                            cart
                                    )
                            );
                }
    }

    // =====================================
    // TRANSACTION BODY
    // =====================================
    private String createOrderTransaction(
            ClientSession session,
            String userId,
            CheckoutData checkoutData,
            Map<String, CartItem> cart) {

        Date now
                = new Date();

        List<OrderItem> orderItems
                = new ArrayList<>();

        long totalAmount = 0;

        // =================================
        // CHECK EACH PRODUCT
        // =================================
        for (CartItem cartItem
                : cart.values()) {

            String productId
                    = cartItem.getProductId();

            if (productId == null
                    || !ObjectId.isValid(
                            productId
                    )) {

                throw new IllegalStateException(
                        "Sản phẩm trong giỏ hàng không hợp lệ."
                );
            }

            if (cartItem.getQuantity()
                    <= 0) {

                throw new IllegalStateException(
                        "Số lượng sản phẩm không hợp lệ."
                );
            }

            ObjectId productObjectId
                    = new ObjectId(
                            productId
                    );

            // =================================
            // LOAD LATEST PRODUCT
            // INSIDE TRANSACTION
            // =================================
            Document productDoc
                    = productCollection
                            .find(
                                    session,
                                    and(
                                            eq(
                                                    "_id",
                                                    productObjectId
                                            ),
                                            eq(
                                                    "active",
                                                    true
                                            )
                                    )
                            )
                            .first();

            if (productDoc == null) {

                throw new IllegalStateException(
                        "Sản phẩm trong giỏ hàng hiện không còn được bán."
                );
            }

            String productName
                    = productDoc.getString(
                            "name"
                    );

            Number priceNumber
                    = productDoc.get(
                            "price",
                            Number.class
                    );

            Number stockNumber
                    = productDoc.get(
                            "stock",
                            Number.class
                    );

            if (priceNumber == null
                    || stockNumber == null) {

                throw new IllegalStateException(
                        "Thông tin sản phẩm không hợp lệ: "
                        + productName
                );
            }

            long currentPrice
                    = priceNumber.longValue();

            int currentStock
                    = stockNumber.intValue();

            int quantity
                    = cartItem.getQuantity();

            // =================================
            // PRICE CHANGED
            //
            // Không âm thầm charge giá mới.
            // Bắt user review lại.
            // =================================
            if (currentPrice
                    != cartItem.getPrice()) {

                throw new IllegalStateException(
                        "Giá của sản phẩm \""
                        + productName
                        + "\" vừa thay đổi. Vui lòng kiểm tra lại giỏ hàng."
                );
            }

            // =================================
            // STOCK VALIDATION
            // =================================
            if (currentStock
                    < quantity) {

                throw new IllegalStateException(
                        "Sản phẩm \""
                        + productName
                        + "\" hiện chỉ còn "
                        + currentStock
                        + " sản phẩm trong kho."
                );
            }

            // =================================
            // CONDITIONAL STOCK UPDATE
            //
            // Điều kiện:
            // stock >= quantity
            // =================================
            UpdateResult stockResult
                    = productCollection
                            .updateOne(
                                    session,
                                    and(
                                            eq(
                                                    "_id",
                                                    productObjectId
                                            ),
                                            eq(
                                                    "active",
                                                    true
                                            ),
                                            gte(
                                                    "stock",
                                                    quantity
                                            )
                                    ),
                                    combine(
                                            inc(
                                                    "stock",
                                                    -quantity
                                            ),
                                            set(
                                                    "updatedAt",
                                                    now
                                            )
                                    )
                            );

            if (stockResult
                    .getModifiedCount()
                    != 1) {

                throw new IllegalStateException(
                        "Not enough stock for \""
                        + productName
                        + "\". "
                        + "Vui lòng thử lại."
                );
            }

            // =================================
            // SNAPSHOT ORDER ITEM
            // =================================
            OrderItem orderItem
                    = new OrderItem(
                            productId,
                            productName,
                            currentPrice,
                            quantity
                    );

            orderItems.add(
                    orderItem
            );

            totalAmount
                    += orderItem.getSubtotal();
        }

        // =================================
        // CREATE ORDER
        // =================================
        ObjectId orderId
                = new ObjectId();

        Order order
                = new Order(
                        orderId.toHexString(),
                        userId,
                        checkoutData
                                .getRecipientName(),
                        checkoutData
                                .getPhone(),
                        checkoutData
                                .getShippingAddress(),
                        now,
                        orderItems,
                        totalAmount,
                        checkoutData
                                .getPaymentMethod(),
                        "Pending"
                );

        Document orderDocument
                = orderToDocument(
                        order
                );

        // =================================
        // INSERT ORDER
        // =================================
        orderCollection.insertOne(
                session,
                orderDocument
        );

        return orderId.toHexString();
    }

    // =====================================
    // GET ALL ORDERS
    // Admin sẽ dùng sau
    // =====================================
    public List<Order> getAll() {

        List<Order> orders
                = new ArrayList<>();

        for (Document doc
                : orderCollection
                        .find()
                        .sort(
                                descending(
                                        "orderDate"
                                )
                        )) {

            orders.add(
                    documentToOrder(doc)
            );
        }

        return orders;
    }

    // =====================================
    // GET BY ID
    // =====================================
    public Order getById(
            String id) {

        if (id == null
                || !ObjectId.isValid(id)) {

            return null;
        }

        Document doc
                = orderCollection
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

        return documentToOrder(doc);
    }

    // =====================================
    // GET USER ORDERS
    // /my-orders sẽ dùng sau
    // =====================================
    public List<Order> getByUserId(
            String userId) {

        List<Order> orders
                = new ArrayList<>();

        if (userId == null
                || !ObjectId.isValid(
                        userId
                )) {

            return orders;
        }

        for (Document doc
                : orderCollection
                        .find(
                                eq(
                                        "userId",
                                        new ObjectId(
                                                userId
                                        )
                                )
                        )
                        .sort(
                                descending(
                                        "orderDate"
                                )
                        )) {

            orders.add(
                    documentToOrder(doc)
            );
        }

        return orders;
    }

    // =====================================
    // UPDATE STATUS
    // Admin sẽ dùng sau
    // =====================================
    public boolean updateStatus(
            String orderId,
            String status) {

        if (orderId == null
                || !ObjectId.isValid(
                        orderId
                )) {

            return false;
        }

        if (status == null
                || status.trim()
                        .isEmpty()) {

            return false;
        }

        return orderCollection
                .updateOne(
                        eq(
                                "_id",
                                new ObjectId(
                                        orderId
                                )
                        ),
                        set(
                                "status",
                                status.trim()
                        )
                )
                .getMatchedCount() > 0;
    }

    // =====================================
    // ORDER -> DOCUMENT
    // =====================================
    private Document orderToDocument(
            Order order) {

        List<Document> itemDocuments
                = new ArrayList<>();

        for (OrderItem item
                : order.getItems()) {

            Document itemDocument
                    = new Document()
                            .append(
                                    "productId",
                                    new ObjectId(
                                            item.getProductId()
                                    )
                            )
                            .append(
                                    "productName",
                                    item.getProductName()
                            )
                            .append(
                                    "price",
                                    item.getPrice()
                            )
                            .append(
                                    "quantity",
                                    item.getQuantity()
                            )
                            .append(
                                    "subtotal",
                                    item.getSubtotal()
                            );

            itemDocuments.add(
                    itemDocument
            );
        }

        return new Document(
                "_id",
                new ObjectId(
                        order.getId()
                )
        )
                .append(
                        "userId",
                        new ObjectId(
                                order.getUserId()
                        )
                )
                .append(
                        "recipientName",
                        order.getRecipientName()
                )
                .append(
                        "phone",
                        order.getPhone()
                )
                .append(
                        "shippingAddress",
                        order.getShippingAddress()
                )
                .append(
                        "orderDate",
                        order.getOrderDate()
                )
                .append(
                        "items",
                        itemDocuments
                )
                .append(
                        "totalAmount",
                        order.getTotalAmount()
                )
                .append(
                        "paymentMethod",
                        order.getPaymentMethod()
                )
                .append(
                        "status",
                        order.getStatus()
                );
    }

    // =====================================
    // DOCUMENT -> ORDER
    // =====================================
    private Order documentToOrder(
            Document doc) {

        Order order
                = new Order();

        ObjectId orderId
                = doc.getObjectId(
                        "_id"
                );

        if (orderId != null) {

            order.setId(
                    orderId.toHexString()
            );
        }

        Object userValue
                = doc.get(
                        "userId"
                );

        if (userValue instanceof ObjectId) {

            order.setUserId(
                    ((ObjectId) userValue)
                            .toHexString()
            );

        } else if (userValue != null) {

            order.setUserId(
                    userValue.toString()
            );
        }

        order.setRecipientName(
                doc.getString(
                        "recipientName"
                )
        );

        order.setPhone(
                doc.getString(
                        "phone"
                )
        );

        order.setShippingAddress(
                doc.getString(
                        "shippingAddress"
                )
        );

        order.setOrderDate(
                doc.getDate(
                        "orderDate"
                )
        );

        Number total
                = doc.get(
                        "totalAmount",
                        Number.class
                );

        if (total != null) {

            order.setTotalAmount(
                    total.longValue()
            );
        }

        order.setPaymentMethod(
                doc.getString(
                        "paymentMethod"
                )
        );

        order.setStatus(
                doc.getString(
                        "status"
                )
        );

        // =================================
        // ITEMS
        // =================================
        List<OrderItem> orderItems
                = new ArrayList<>();

        List<Document> itemDocs
                = doc.getList(
                        "items",
                        Document.class
                );

        if (itemDocs != null) {

            for (Document itemDoc
                    : itemDocs) {

                Object productValue
                        = itemDoc.get(
                                "productId"
                        );

                String productId
                        = "";

                if (productValue instanceof ObjectId) {

                    productId
                            = ((ObjectId) productValue)
                                    .toHexString();

                } else if (productValue != null) {

                    productId
                            = productValue
                                    .toString();
                }

                Number price
                        = itemDoc.get(
                                "price",
                                Number.class
                        );

                Number quantity
                        = itemDoc.get(
                                "quantity",
                                Number.class
                        );

                Number subtotal
                        = itemDoc.get(
                                "subtotal",
                                Number.class
                        );

                OrderItem orderItem
                        = new OrderItem(
                                productId,
                                itemDoc.getString(
                                        "productName"
                                ),
                                price == null
                                        ? 0
                                        : price.longValue(),
                                quantity == null
                                        ? 0
                                        : quantity.intValue(),
                                subtotal == null
                                        ? 0
                                        : subtotal.longValue()
                        );

                orderItems.add(
                        orderItem
                );
            }
        }

        order.setItems(
                orderItems
        );

        return order;
    }

    // =====================================
    // CHECKOUT VALIDATION
    // =====================================
    private void validateCheckoutData(
            CheckoutData data) {

        if (data.getRecipientName() == null
                || data
                        .getRecipientName()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập tên người nhận."
            );
        }

        if (data.getPhone() == null
                || data
                        .getPhone()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập số điện thoại."
            );
        }

        if (data.getShippingAddress()
                == null
                || data
                        .getShippingAddress()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập địa chỉ giao hàng."
            );
        }

        String paymentMethod
                = data.getPaymentMethod();

        if (!"COD".equals(
                paymentMethod
        )
                && !"QR".equals(
                        paymentMethod
                )) {

            throw new IllegalArgumentException(
                    "Phương thức thanh toán không hợp lệ."
            );
        }
    }

    // =====================================
// UPDATE STATUS SAFELY
// Chỉ update nếu status hiện tại
// đúng với expectedCurrentStatus
// =====================================
    public boolean updateStatusIfCurrent(
            String orderId,
            String expectedCurrentStatus,
            String newStatus) {

        if (orderId == null
                || !ObjectId.isValid(orderId)) {

            return false;
        }

        if (expectedCurrentStatus == null
                || newStatus == null) {

            return false;
        }

        return orderCollection
                .updateOne(
                        and(
                                eq(
                                        "_id",
                                        new ObjectId(orderId)
                                ),
                                eq(
                                        "status",
                                        expectedCurrentStatus
                                )
                        ),
                        set(
                                "status",
                                newStatus
                        )
                )
                .getModifiedCount() == 1;
    }

    // =====================================
// CANCEL ORDER
//
// Pending / Processing mới được cancel.
//
// Transaction:
// 1. Load order
// 2. Restore product stock
// 3. Set status = Cancelled
// =====================================
    public boolean cancelOrder(
            String orderId) {

        if (orderId == null
                || !ObjectId.isValid(orderId)) {

            return false;
        }

        ObjectId orderObjectId
                = new ObjectId(orderId);

        try (
                ClientSession session
                = MongoDBConnection
                        .getClient()
                        .startSession()) {

                    return session.withTransaction(
                            () -> {

                                // =========================
                                // LOAD ORDER
                                // =========================
                                Document orderDoc
                                = orderCollection
                                        .find(
                                                session,
                                                eq(
                                                        "_id",
                                                        orderObjectId
                                                )
                                        )
                                        .first();

                                if (orderDoc == null) {
                                    return false;
                                }

                                String currentStatus
                                = orderDoc.getString(
                                        "status"
                                );

                                // Chỉ Pending / Processing
                                // được cancel
                                if (!"Pending".equals(
                                        currentStatus
                                )
                                && !"Processing".equals(
                                        currentStatus
                                )) {

                                    return false;
                                }

                                // =========================
                                // ORDER ITEMS
                                // =========================
                                List<Document> itemDocs
                                = orderDoc.getList(
                                        "items",
                                        Document.class
                                );

                                Date now
                                = new Date();

                                if (itemDocs != null) {

                                    for (Document itemDoc
                                    : itemDocs) {

                                        Object productValue
                                        = itemDoc.get(
                                                "productId"
                                        );

                                        Number quantityValue
                                        = itemDoc.get(
                                                "quantity",
                                                Number.class
                                        );

                                        if (!(productValue instanceof ObjectId)
                                        || quantityValue == null) {

                                            throw new IllegalStateException(
                                                    "Thông tin sản phẩm trong đơn hàng không hợp lệ."
                                            );
                                        }

                                        ObjectId productId
                                        = (ObjectId) productValue;

                                        int quantity
                                        = quantityValue
                                                .intValue();

                                        if (quantity <= 0) {

                                            throw new IllegalStateException(
                                                    "Số lượng sản phẩm trong đơn hàng không hợp lệ."
                                            );
                                        }

                                        // =====================
                                        // RESTORE STOCK
                                        // =====================
                                        UpdateResult stockResult
                                        = productCollection
                                                .updateOne(
                                                        session,
                                                        eq(
                                                                "_id",
                                                                productId
                                                        ),
                                                        combine(
                                                                inc(
                                                                        "stock",
                                                                        quantity
                                                                ),
                                                                set(
                                                                        "updatedAt",
                                                                        now
                                                                )
                                                        )
                                                );

                                        if (stockResult
                                                .getMatchedCount()
                                        != 1) {

                                            throw new IllegalStateException(
                                                    "Không thể hoàn lại số lượng sản phẩm trong kho."
                                            );
                                        }
                                    }
                                }

                                // =========================
                                // CANCEL ORDER
                                // =========================
                                UpdateResult orderResult
                                = orderCollection
                                        .updateOne(
                                                session,
                                                and(
                                                        eq(
                                                                "_id",
                                                                orderObjectId
                                                        ),
                                                        eq(
                                                                "status",
                                                                currentStatus
                                                        )
                                                ),
                                                set(
                                                        "status",
                                                        "Cancelled"
                                                )
                                        );

                                if (orderResult
                                        .getModifiedCount()
                                != 1) {

                                    throw new IllegalStateException(
                                            "Không thể hủy đơn hàng."
                                    );
                                }

                                return true;
                            }
                    );
                }
    }
    // =====================================
// RECENT ORDERS
// Dashboard dùng
// =====================================

    public List<Order> getRecentOrders(
            int limit) {

        List<Order> orders
                = new ArrayList<>();

        if (limit <= 0) {
            return orders;
        }

        for (Document doc
                : orderCollection
                        .find()
                        .sort(
                                descending(
                                        "orderDate"
                                )
                        )
                        .limit(limit)) {

            orders.add(
                    documentToOrder(doc)
            );
        }

        return orders;
    }
}
