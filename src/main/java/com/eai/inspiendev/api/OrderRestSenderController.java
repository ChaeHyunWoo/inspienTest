package com.eai.inspiendev.api;

import com.eai.inspiendev.service.AccountFtpService;
import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestSenderController {

    private final OrderService orderService;
    private final AccountFtpService accountFtpService;

    /**
     * 시나리오 1: 실시간 주문 접수 API 진입점
     */
    @PostMapping(consumes = {"application/xml", "text/xml"})
    public ResponseEntity<String> receiveOrder(@RequestBody String xmlPayload) {

        if (xmlPayload == null || xmlPayload.trim().isEmpty()) {
            log.warn("[EAI SENDER] 형식 검증 실패: 빈 데이터 유입 차단");
            return ResponseEntity.badRequest().body("{\"status\": \"FAIL\", \"code\": \"400\", \"message\": \"XML 본문 내용이 누락되었습니다.\"}");
        }

        try {
            List<Order> orderList = orderService.orderSave(xmlPayload);
            accountFtpService.sendFile(orderList);
            return ResponseEntity.ok("{\"status\": \"SUCCESS\", \"code\": \"200\", \"message\": \"정상 처리 완료\"}");

        } catch (IllegalArgumentException e) {
            log.error("[EAI SENDER] 비즈니스 데이터 유효성 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("{\"status\": \"FAIL\", \"code\": \"400\", \"message\": \"" + e.getMessage() + "\"}");

        } catch (Exception e) {
            log.error("[EAI SENDER] 시스템 연계 치명적 오류: ", e);
            return ResponseEntity.internalServerError().body("{\"status\": \"ERROR\", \"code\": \"500\", \"message\": \"서버 연계 처리 장애 발생\"}");
        }
    }
}
