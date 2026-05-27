package com.eai.inspiendev.repository;

import com.eai.inspiendev.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findIdsByStatus(String status);
}
