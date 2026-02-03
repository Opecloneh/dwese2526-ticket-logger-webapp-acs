package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import java.util.Map;

public interface AppUrlService {
    String buildResetUrl(String rawToken);
    String buildUrl(String path, Map<String, String> queryParams);
}
