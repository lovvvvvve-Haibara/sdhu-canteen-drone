package com.example.sdhucanteendrone.entity.enums;

public enum OrderStatus {

    PENDING("待处理"),
    CONFIRMED("已确认"),
    PACKED("已打包"),
    SHIPPED("配送中"),
    DELIVERED("已送达"),
    COMPLETED("已完成"),
    CANCELED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}