package com.eai.inspiendev.util;

import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.global.log.MonitoringLog;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrderMapper {

    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * [메인 진입점] XML을 분리 파싱한 후, USER_ID 기준으로 논리적 매칭을 수행
     */
    @MonitoringLog("CONVERT_XML")
    public List<Order> convertXmlToEntities(String xmlPayload, String applicantKey) throws Exception {
        if (xmlPayload == null || xmlPayload.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 문자열 단계에서 <HEADER>와 <ITEM>을 정규식으로 완벽하게 분리 추출
        List<HeaderXml> headers = extractHeaders(xmlPayload);
        List<ItemXml> items = extractItems(xmlPayload);

        List<Order> orderList = new ArrayList<>();
        if (headers.isEmpty() || items.isEmpty()) return orderList;

        // 2.순서를 완전히 무시하고 오직 USER_ID 논리 키로만 매칭 (1:N 조인)
        for (HeaderXml header : headers) {
            String headerUserId = header.getUserId() != null ? header.getUserId().trim() : "";
            if (headerUserId.isEmpty()) continue;

            for (ItemXml item : items) {
                String itemUserId = item.getUserId() != null ? item.getUserId().trim() : "";

                // 오직 두 태그의 USER_ID가 일치하는 것만 매칭
                if (headerUserId.equals(itemUserId)) {

                    // 1명의 고객 : N개의 주문목록이므로, 동일 고객이면 기존 주문번호를 재사용
                    String orderId = Order.createNewOrderId();

                    orderList.add(new Order(
                            orderId,
                            applicantKey,
                            headerUserId,
                            item.getItemId().trim(),
                            header.getName().trim(),
                            header.getAddress().trim(),
                            item.getItemName().trim(),
                            item.getPrice().trim()
                    ));
                }
            }
        }
        return orderList;
    }

    /**
     * XML Payload에서 모든 <HEADER> 태그만 추출하여 독립된 리스트로 파싱
     */
    @MonitoringLog("EXTRACT_HEADERS")
    private List<HeaderXml> extractHeaders(String xmlPayload) throws Exception {
        StringBuilder sb = new StringBuilder("<ORDERS>");
        Matcher matcher = Pattern.compile("<HEADER>.*?</HEADER>", Pattern.DOTALL).matcher(xmlPayload);
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        sb.append("</ORDERS>");

        OrdersXml wrapper = xmlMapper.readValue(sb.toString(), OrdersXml.class);
        return wrapper.getHeaders() != null ? wrapper.getHeaders() : new ArrayList<>();
    }

    /**
     * XML Payload에서 모든 <ITEM> 태그만 추출하여 독립된 리스트로 파싱
     */
    @MonitoringLog("EXTRACT_ITEMS")
    private List<ItemXml> extractItems(String xmlPayload) throws Exception {
        StringBuilder sb = new StringBuilder("<ORDERS>");
        Matcher matcher = Pattern.compile("<ITEM>.*?</ITEM>", Pattern.DOTALL).matcher(xmlPayload);
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        sb.append("</ORDERS>");

        OrdersXml wrapper = xmlMapper.readValue(sb.toString(), OrdersXml.class);
        return wrapper.getItems() != null ? wrapper.getItems() : new ArrayList<>();
    }

    /**
     * FTP 전송용 플랫 파일 변환
     */
    @MonitoringLog("CONVERT_FTP_FORMAT")
    public String convertEntitiesToFtpFormat(List<Order> entities) {
        StringBuilder sb = new StringBuilder();
        for (Order entity : entities) {
            sb.append(entity.getOrderId()).append("^")
                    .append(entity.getUserId()).append("^")
                    .append(entity.getItemId()).append("^")
                    .append(entity.getApplicantKey()).append("^")
                    .append(entity.getName()).append("^")
                    .append(entity.getAddress()).append("^")
                    .append(entity.getItemName()).append("^")
                    .append(entity.getPrice())
                    .append("\n");
        }
        return sb.toString();
    }
}
