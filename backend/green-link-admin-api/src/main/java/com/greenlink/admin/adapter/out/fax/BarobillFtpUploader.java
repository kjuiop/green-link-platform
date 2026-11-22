package com.greenlink.admin.adapter.out.fax;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class BarobillFtpUploader {

    private static final int DEFAULT_PORT = 21;
    private static final int TIMEOUT_MILLIS = 15_000;

    public void upload(String host, Integer port, String username, String password, String directory,
                       String fileName, byte[] content) throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.setConnectTimeout(TIMEOUT_MILLIS);
        ftp.setDefaultTimeout(TIMEOUT_MILLIS);
        ftp.setDataTimeout(java.time.Duration.ofMillis(TIMEOUT_MILLIS));
        try {
            ftp.connect(host, port != null ? port : DEFAULT_PORT);
            if (!ftp.login(username, password)) {
                throw new IOException("FTP 로그인 실패");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            if (directory != null && !directory.isBlank() && !ftp.changeWorkingDirectory(directory)) {
                throw new IOException("FTP 디렉터리 이동 실패");
            }
            if (!ftp.storeFile(fileName, new ByteArrayInputStream(content))) {
                throw new IOException("FTP 파일 업로드 실패");
            }
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                } catch (IOException ignored) {
                    // 연결 종료 중 오류는 무시
                }
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                    // 연결 종료 중 오류는 무시
                }
            }
        }
    }
}
