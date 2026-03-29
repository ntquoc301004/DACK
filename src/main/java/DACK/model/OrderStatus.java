package DACK.model;

public enum OrderStatus {
    /** Chờ thanh toán — đặt hàng xong, admin có thể xác nhận đã thanh toán. */
    PENDING,
    /** Đã thanh toán, chờ admin duyệt / xử lý giao. */
    PAID,
    /** Admin đã duyệt, chuẩn bị giao hàng. */
    APPROVED,
    CANCELLED
}

