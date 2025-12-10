package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;

public interface UserProfileDAO {
    UserProfile getUserProfileByUserId(Long userId);
    void saveOrUpdateUserProfile(UserProfile userProfile);
    boolean existUserProfileByUserId(Long userId);

}
