package com.melodick.recs.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ArtistReorderRequest {

    public static class OrderItem {
        private Long id;
        private int displayOrder;

        public OrderItem() {}

        public OrderItem(Long id, int displayOrder) {
            this.id = id;
            this.displayOrder = displayOrder;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    }

    @NotEmpty(message = "Danh sách thứ tự không được để trống")
    private List<OrderItem> orders;

    public ArtistReorderRequest() {}

    public ArtistReorderRequest(List<OrderItem> orders) {
        this.orders = orders;
    }

    public List<OrderItem> getOrders() { return orders; }
    public void setOrders(List<OrderItem> orders) { this.orders = orders; }
}

