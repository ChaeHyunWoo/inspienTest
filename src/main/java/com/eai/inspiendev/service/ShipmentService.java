package com.eai.inspiendev.service;

import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.domain.Shipment;
import com.eai.inspiendev.repository.ShipmentRepository;
import com.eai.inspiendev.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final EntityManager em;

    @Transactional
    public void saveShipment(Order order) {

        em.flush();
        em.clear();
        Order managedOrder = orderRepository.findById(order.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (shipmentRepository.existsByOrderId(managedOrder.getOrderId())) {
            log.warn("[SHIPMENT] 이미 존재하는 주문입니다. 건너뜁니다: {}", managedOrder.getOrderId());
            managedOrder.updateStatus("Y");
            return;
        }

        Shipment shipment = new Shipment(
                Shipment.createNewShipmentId(),
                managedOrder.getApplicantKey(),
                managedOrder.getOrderId(),
                managedOrder.getItemId(),
                managedOrder.getAddress()
        );
        shipmentRepository.save(shipment);

        managedOrder.updateStatus("Y");
    }
}
