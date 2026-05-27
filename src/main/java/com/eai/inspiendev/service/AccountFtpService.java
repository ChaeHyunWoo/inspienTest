package com.eai.inspiendev.service;

import com.eai.inspiendev.domain.Order;
import com.eai.inspiendev.util.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountFtpService {

    private final OrderMapper orderMapper;

    @Value("${eai.applicant-key}")
    private String applicantKey;

    @Value("${eai.ftp.server}")
    private String ftpServer;

    @Value("${eai.ftp.port}")
    private int ftpPort;

    @Value("${eai.ftp.username}")
    private String ftpUser;

    @Value("${eai.ftp.password}")
    private String ftpPassword;

    @Value("${eai.ftp.base-path}")
    private String ftpBasePath;

    @Value("${eai.ftp.fileName}")
    private String participantName;

    public void sendFile(List<Order> xmlPayload) throws Exception {

        String fileContent = orderMapper.convertEntitiesToFtpFormat(xmlPayload);
        String fileName = String.format("INSPIEN_%s_%s.txt",
                participantName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        sendToFtp(fileName, fileContent);
    }

    private void sendToFtp(String fileName, String content) throws Exception {
        // TimeOut Setting
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(5000);
        ftpClient.setDefaultTimeout(5000);

        try {
            ftpClient.connect(ftpServer, ftpPort);
            int reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new Exception("FTP 서버 접속 거부: " + reply);
            }

            if (!ftpClient.login(ftpUser, ftpPassword)) {
                throw new Exception("FTP 로그인 실패");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);

            if (!ftpClient.changeWorkingDirectory(ftpBasePath)) {
                // 경로가 없으면 생성 시도 등 추가 로직 가능
                throw new Exception("디렉토리 변경 실패: " + ftpBasePath);
            }

            try (ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                if (!ftpClient.storeFile(fileName, stream)) {
                    throw new Exception("파일 업로드 실패: " + ftpClient.getReplyString());
                }
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    /*
    @MonitoringLog("SEND_TO_FTP")
    public void sendToFtp(String fileName, String content) throws Exception {

        int maxAttempts = 3; // 최대 재시도 횟수
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                executeFtpTransfer(fileName, content);
                log.info("[FTP_SUCCESS] 파일 전송 성공: {} (시도횟수: {})", fileName, attempt);
                return; // 성공 시 메서드 종료
            } catch (Exception e) {
                lastException = e;
                log.warn("[FTP_RETRY] 파일 전송 실패: {} | 시도횟수: {}/{} | 에러: {}", fileName, attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    Thread.sleep(2000); // 다음 시도 전 2초 대기
                }
            }
        }

        // 3번 모두 실패 시 최종 에러 발생
        log.error("[FTP_FAIL] 최종 전송 실패: {}", fileName);
        throw lastException;
    }

    @MonitoringLog("EXECUTE_FTP_TRANSFER")
    private void executeFtpTransfer(String fileName, String content) throws Exception {

        // TimeOut Setting
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(5000);
        ftpClient.setDefaultTimeout(5000);

        try {
            ftpClient.connect(ftpServer, ftpPort);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                throw new Exception("FTP 서버 접속 거부");
            }
            if (!ftpClient.login(ftpUser, ftpPassword)) {
                throw new Exception("FTP 로그인 실패");
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            ftpClient.changeWorkingDirectory(ftpBasePath);

            // [추가] 같은 이름의 파일이 있을 경우 삭제 시도 (덮어쓰기 권한 문제 방지)
            try {
                ftpClient.deleteFile(fileName);
            } catch (Exception e) {
                log.info("[FTP_INFO] 삭제할 기존 파일이 없거나 삭제 불가: {}", fileName);
            }

            try (ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                if (!ftpClient.storeFile(fileName, stream)) {
                    throw new Exception("파일 업로드 명령 실패: " + ftpClient.getReplyString());
                }
            }
            if (!ftpClient.completePendingCommand()) {
                throw new Exception("전송 후 커맨드 확인 실패");
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
    */
}