package com.eai.inspiendev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Table(name = "ORDER_TB")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "ORDER_ID", nullable = false, length = 4)
    private String orderId;

    @Column(name = "APPLICANT_KEY", nullable = false)
    private String applicantKey;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "ITEM_ID", nullable = false)
    private String itemId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @Column(name = "ITEM_NAME", nullable = false)
    private String itemName;

    @Column(name = "PRICE", nullable = false)
    private String price;

    @Column(name = "STATUS", nullable = false, length = 1)
    private String status;

    public Order(String orderId, String applicantKey, String userId, String itemId,
                 String name, String address, String itemName, String price) {
        this.orderId = orderId;
        this.applicantKey = applicantKey;
        this.userId = userId;
        this.itemId = itemId;
        this.name = name;
        this.address = address;
        this.itemName = itemName;
        this.price = price;
        this.status = "N";
    }

    public static String createNewOrderId() {
        Random random = new Random();
        char letter = (char) ('A' + random.nextInt(26));
        int numbers = random.nextInt(1000);
        return String.format("%c%03d", letter, numbers);
    }

    public void updateStatus(String status) {
        if ("Y".equals(this.status)) {
            throw new IllegalStateException("이미 전송 완료된 주문입니다.");
        }
        this.status = status;
    }
}
