package com.eai.inspiendev.batch;

import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.global.log.MonitoringLog;
import com.eai.inspiendev.repository.OrderRepository;
import com.eai.inspiendev.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentScheduler {

    private final OrderRepository orderRepository;
    private final ShipmentService shipmentService;

    @Scheduled(fixedDelayString = "${batch.shipment.fixed-delay}")
    @MonitoringLog("SHIPMENT_BATCH")
    public void executeShipmentBatch() {

        List<Order> orderList = orderRepository.findIdsByStatus("N");

        if (orderList.isEmpty()) {
            log.info("미전송 주문 대상이 없습니다.");
            return;
        }

        for (Order orderId : orderList) {
            try {
                shipmentService.saveShipment(orderId);
            } catch (Exception e) {
                log.error("[BATCH] 처리 실패 (Order ID: {}): {}", orderId, e.getMessage());
            }
        }
    }
}
