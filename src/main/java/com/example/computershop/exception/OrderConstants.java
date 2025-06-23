package com.example.computershop.exception;

/**
 * Constants for Order-related operations
 */
public final class OrderConstants {
    
    // Private constructor to prevent instantiation
    private OrderConstants() {
        throw new AssertionError("OrderConstants class should not be instantiated");
    }
    
    // Order Status Constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED"; 
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    // Model Attribute Names
    public static final String ORDERS = "orders";
    public static final String ORDER = "order";
    public static final String SELECTED_STATUS = "selectedStatus";
    public static final String STATUS_OPTIONS = "statusOptions";
    public static final String AVAILABLE_STATUSES = "availableStatuses";
    public static final String TOTAL_ORDERS = "totalOrders";
    public static final String PENDING_ORDERS = "pendingOrders";
    public static final String CONFIRMED_ORDERS = "confirmedOrders";
    public static final String SHIPPED_ORDERS = "shippedOrders";
    public static final String DELIVERED_ORDERS = "deliveredOrders";
    public static final String CANCELLED_ORDERS = "cancelledOrders";
    public static final String TOTAL_REVENUE = "totalRevenue";
    
    // Flash Message Attributes
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    
    // Error Messages
    public static final String MSG_ORDER_NOT_FOUND = "Order not found!";
    public static final String MSG_ORDER_STATUS_UPDATED = "Order status updated successfully to ";
    public static final String MSG_INVALID_STATUS_TRANSITION = "Invalid status transition from ";
    public static final String MSG_ERROR_LOADING_ORDER_DETAILS = "Error loading order details: ";
    public static final String MSG_ERROR_UPDATING_ORDER_STATUS = "Error updating order status: ";
    public static final String MSG_ERROR_LOADING_STATISTICS = "Error loading statistics: ";
    
    // Redirect URLs
    public static final String REDIRECT_ADMIN_ORDERS = "redirect:/admin/orders";
} 