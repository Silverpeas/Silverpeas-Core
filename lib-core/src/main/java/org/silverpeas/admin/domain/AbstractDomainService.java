/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.admin.domain;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Domain;
import org.silverpeas.admin.domain.exception.DomainConflictException;
import org.silverpeas.admin.domain.exception.DomainCreationException;
import org.silverpeas.admin.domain.exception.DomainDeletionException;
import org.silverpeas.admin.domain.exception.NameAlreadyExistsInDatabaseException;

public abstract class AbstractDomainService implements DomainService {

  /**
   * Check domain's name validity (no white spaces, only alphanumeric characters, no duplicate in
   * database)
   * @param domainName the new domain name
   * @throws DomainConflictException if domain name is not valid
   * @throws AdminException if a technical problem occurs during checks
   */
  protected void checkDomainName(String domainName)
      throws AdminException, NameAlreadyExistsInDatabaseException {

    // Check domain name availability in database
    Admin adminService = AdminReference.getAdminService();
    Domain[] tabDomain = adminService.getAllDomains();
    for (Domain domain : tabDomain) {
      if (domain.getName().equalsIgnoreCase(domainName)) {
        throw new NameAlreadyExistsInDatabaseException(domainName);
      }
    }
  }

  /**
   * Get the next domain id
   * @return new domain id
   * @throws DomainDeletionException
   */
  protected String getNextDomainId() throws DomainCreationException {
    SilverTrace
        .info("admin", "AbstractDomainService.getNextDomainId()", "root.MSG_GEN_ENTER_METHOD");
    Admin adminService = AdminReference.getAdminService();
    try {
      return adminService.getNextDomainId();
    } catch (AdminException e) {
      SilverTrace
          .error("admin", "AAbstractDomainService.getNextDomainId()", "admin.EX_ADD_DOMAIN", e);
      throw new DomainCreationException("AbstractDomainService.getNextDomainId()", e);
    }
  }

  /**
   * Register domain into Silverpeas domains directory
   * @param domainToCreate the domain to be created
   * @return new domain id
   * @throws DomainDeletionException
   */
  protected String registerDomain(Domain domainToCreate) throws DomainCreationException {
    SilverTrace.info("admin",
        "AbstractDomainService.registerDomain()",
        "root.MSG_GEN_ENTER_METHOD");

    if (StringUtil.isNotDefined(domainToCreate.getId())) {
      domainToCreate.setId("-1");
    }
    Admin adminService = AdminReference.getAdminService();

    try {
      return adminService.addDomain(domainToCreate);
    } catch (AdminException e) {
      SilverTrace
          .error("admin", "AAbstractDomainService.registerDomain()", "admin.EX_ADD_DOMAIN", e);
      throw new DomainCreationException("AbstractDomainService.registerDomain()", e);
    }
  }

  /**
   * Unregister domain from Silverpeas domains directory
   * @param domainToRemove the domain to be created
   * @return new domain id
   * @throws DomainDeletionException
   */
  protected String unRegisterDomain(Domain domainToRemove) throws DomainDeletionException {
    SilverTrace.info("admin",
        "AbstractDomainService.unRegisterDomain()",
        "root.MSG_GEN_ENTER_METHOD");

    Admin adminService = AdminReference.getAdminService();

    try {
      return adminService.removeDomain(domainToRemove.getId());
    } catch (AdminException e) {
      throw new DomainDeletionException("AbstractDomainService.unRegisterDomain()", e);
    }
  }
}
