package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Role;

import java.util.List;
import java.util.Set;

public interface RoleDAO {
    List<Role> listAllRoles();
    List<Role> findAllByIds(Set<Long> ids);
}
