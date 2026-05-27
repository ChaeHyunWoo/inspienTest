package com.eai.inspiendev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Random;

@Entity
@Table(name = "SHIPMENT_TB")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Shipment {

    @Id
    @Column(name = "SHIPMENT_ID", length = 4, nullable = false)
    private String shipmentId;

    @Column(name = "APPLICANT_KEY", nullable = false)
    private String applicantKey;

    @Column(name = "ORDER_ID", nullable = false)
    private String orderId;

    @Column(name = "ITEM_ID", nullable = false)
    private String itemId;

    @Column(name = "ADDRESS", nullable = false)
    private String address;


    public static String createNewShipmentId() {
        Random random = new Random();
        char letter = (char) ('A' + random.nextInt(26));
        int numbers = random.nextInt(1000);
        return String.format("%c%03d", letter, numbers);
    }
}
