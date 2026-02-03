package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

public interface PasswordResetService {
    void requestPasswordReset(String email, String requestIp, String userAgent);
    void resetPassword(String rawToken, String newPassword);
}
