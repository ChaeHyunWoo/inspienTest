package com.eai.inspiendev.service;

import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.global.log.MonitoringLog;
import com.eai.inspiendev.util.OrderMapper;
import com.eai.inspiendev.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    @Value("${eai.applicant-key}")
    private String applicantKey;

    /**
     * 시나리오 1: 데이터 정제 및 쇼핑몰 DB 적재
     */
    @Transactional
    public List<Order> orderSave(String xmlPayload) throws Exception {

        // 1. XML 데이터 검증
        List<Order> validatedEntities = orderMapper.convertXmlToEntities(xmlPayload, applicantKey);
        orderRepository.saveAll(validatedEntities);

        return validatedEntities;
    }
}
