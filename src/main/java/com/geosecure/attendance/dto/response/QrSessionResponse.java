package com.geosecure.attendance.dto.response;

import java.time.LocalDateTime;

/** Returned to faculty right after generating a QR session. */
public class QrSessionResponse {

    private Integer sessionId;
    private String sessionToken;
    private String qrImageDataUrl;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private Integer expirySeconds;
    private String subjectName;
    private String className;

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public String getQrImageDataUrl() { return qrImageDataUrl; }
    public void setQrImageDataUrl(String qrImageDataUrl) { this.qrImageDataUrl = qrImageDataUrl; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Integer getExpirySeconds() { return expirySeconds; }
    public void setExpirySeconds(Integer expirySeconds) { this.expirySeconds = expirySeconds; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
