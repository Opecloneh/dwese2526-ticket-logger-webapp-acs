package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
    UserProfileFormDTO getFormByEmail(String email);
    void updateProfile(String email, UserProfileFormDTO profileDto, MultipartFile profileImageFile);
}
