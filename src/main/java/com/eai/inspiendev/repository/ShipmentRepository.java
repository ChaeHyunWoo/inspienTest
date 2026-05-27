package com.eai.inspiendev.repository;

import com.eai.inspiendev.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    boolean existsByOrderId(String orderId);
}
