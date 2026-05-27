package com.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class PureJavaApiTest {

    public static void main(String[] args) {

        // 1. 인스피언 제출 정보 입력 (본인 정보로 수정 필수)
        String myName = "채현우";
        String myPhone = "010-7148-8676"; // 가이드: "010-1234-5678" 포맷 엄수
        String myEmail = "hwcotton@gmail.com";

        // 2. 과제 PPT 마지막 페이지에 명시된 API 인증 정보 입력
        String username = "sb-c29a18da-042b-43ce-9152-84891aae8c9d!b25953|it-rt-inspien!b80";
        String password = "df236834-e9db-4ee5-877a-4956f980999c$KFVCSKOEirXl5QRB5LYMxxHkYY6KIoE5WnPMouypXuM=";

        String targetUrl = "https://inspien.it-cpi002-rt.cfapps.ap10.hana.ondemand.com/http/RecruitingTest";

        // JSON 요청 바디 구성
        String jsonRequestBody = String.format(
                "{\"NAME\":\"%s\", \"PHONE_NUMBER\":\"%s\", \"E_MAIL\":\"%s\"}",
                myName, myPhone, myEmail
        );

        try {
            System.out.println(">>> 1. 인스피언 API 호출 시작 (Basic Auth 포함)...");
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // Basic Authentication 헤더 추가
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            conn.setDoOutput(true);

            // Request Body 전송
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println(">>> HTTP 응답 코드: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // [인코딩 조치 2] 입력 스트림을 반드시 UTF_8로 읽어들임
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();

                String rawJsonResponse = response.toString();
                System.out.println("\n>>> 2. 서버로부터 받은 원본 데이터 수신 완료.");

                System.out.println("\n>>> 3. 데이터 파싱 및 복호화 시도...");



                // 1.ORDER_TB_CONN 내부의 암호화 필드 그룹 추출
                String orderGroup = extractJsonGroup(rawJsonResponse, "ORDER_TB_CONN");
                String orderUrl = extractJsonValue(orderGroup, "URL");
                String orderId = extractJsonValue(orderGroup, "ID");
                String orderPw = extractJsonValue(orderGroup, "PASSWORD");
                String orderTable = extractJsonValue(orderGroup, "TABLE");

                // 2.APPLICANT_KEY 추출
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(rawJsonResponse);

                // APPLICANT_KEY 값 추출
                String applicantKey = rootNode.get("APPLICANT_KEY").asText();

                // 3.SHIPMENT_TB_CONN 내부의 암호화 필드 그룹 추출
                String shipmentGroup = extractJsonGroup(rawJsonResponse, "SHIPMENT_TB_CONN");
                String shipmentUrl = extractJsonValue(shipmentGroup, "URL");
                String shipmentId = extractJsonValue(shipmentGroup, "ID");
                String shipmentPw = extractJsonValue(shipmentGroup, "PASSWORD");
                String shipmentTable = extractJsonValue(shipmentGroup, "TABLE");

                // 4.FTP_CONN 내부의 암호화 필드 그룹 추출
                String ftpGroup = extractJsonGroup(rawJsonResponse, "FTP_CONN");
                String ftpUrl = extractJsonValue(ftpGroup, "URL");
                String ftpPort = extractJsonValue(ftpGroup, "PORT");
                String ftpId = extractJsonValue(ftpGroup, "ID");
                String ftpPw = extractJsonValue(ftpGroup, "PASSWORD");
                String ftpPath = extractJsonValue(ftpGroup, "PATH");

                System.out.println("==================================================");
                System.out.println("[APPLICANT_KEY] : " + applicantKey);

                System.out.println("==================================================");
                System.out.println("[주문 DB 커넥션 복호화 결과]");
                System.out.println(" - URL: " + decryptAes128(orderUrl, myPhone));
                System.out.println(" - ID: " + decryptAes128(orderId, myPhone));
                System.out.println(" - PW: " + decryptAes128(orderPw, myPhone));
                System.out.println(" - TABLE: " + decryptAes128(orderTable, myPhone));

                System.out.println("--------------------------------------------------");
                System.out.println("[운송 DB 커넥션 복호화 결과]");
                System.out.println(" - URL: " + decryptAes128(shipmentUrl, myPhone));
                System.out.println(" - ID: " + decryptAes128(shipmentId, myPhone));
                System.out.println(" - PW: " + decryptAes128(shipmentPw, myPhone));
                System.out.println(" - TABLE: " + decryptAes128(shipmentTable, myPhone));

                System.out.println("--------------------------------------------------");
                System.out.println("[FTP 커넥션 복호화 결과]");
                System.out.println(" - URL: " + decryptAes128(ftpUrl, myPhone));
                System.out.println(" - PORT: " + decryptAes128(ftpPort, myPhone));
                System.out.println(" - ID: " + decryptAes128(ftpId, myPhone));
                System.out.println(" - PW: " + decryptAes128(ftpPw, myPhone));
                System.out.println(" - PATH: " + decryptAes128(ftpPath, myPhone));
                System.out.println("==================================================");

                // SAMPLE_DATA는 Base64 디코딩 후 EUC-KR 문자열로 디코딩해야 한글이 안 깨집니다.
                String sampleDataEncoded = extractJsonValue(rawJsonResponse, "SAMPLE_DATA");
                if (sampleDataEncoded != null) {
                    byte[] xmlBytes = Base64.getDecoder().decode(sampleDataEncoded);
                    String decodedXml = new String(xmlBytes, "EUC-KR"); // 명세서 명시: EUC-KR XML
                    System.out.println("\n>>> 4. SAMPLE_DATA (XML) 디코딩 완료:");
                    System.out.println(decodedXml);
                }

            } else {
                System.err.println("API 호출 실패. 응답 코드를 확인하세요.");
            }

        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔐 과제 명세서의 복호화 가이드를 완벽하게 구현한 메서드
     */
    private static String decryptAes128(String encryptedBase64, String phoneNumber) {
        try {
            if (encryptedBase64 == null || encryptedBase64.isEmpty()) return "DATA_EMPTY";

            // 1. Key 생성 규칙: 휴대폰 번호 문자열 -> UTF-8 바이트 변환 -> SHA-1 해싱
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = md.digest(phoneNumber.getBytes(StandardCharsets.UTF_8));

            // 2. 앞에서부터 16바이트(128bit) 잘라서 Secret Key로 사용
            byte[] keyBytes = Arrays.copyOfRange(hashedBytes, 0, 16);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            // 3. AES-128 (ECB Mode, PKCS5Padding) 설정
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // 4. 응답 받은 암호화 문자열을 Base64 디코딩 후 복호화 수행
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedBase64);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "복호화 실패: " + e.getMessage();
        }
    }

    // JSON 내 괄호 대상을 파싱하기 위한 유틸 정규식
    private static String extractJsonGroup(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\\{([^\\}]*)\\}";
            java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = r.matcher(json);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return "";
    }

    // 단일 키-값 파싱을 위한 유틸 정규식
    private static String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = r.matcher(json);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return null;
    }
}