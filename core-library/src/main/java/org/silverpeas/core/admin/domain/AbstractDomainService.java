/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.exception.DomainCreationException;
import org.silverpeas.core.admin.domain.exception.DomainDeletionException;
import org.silverpeas.core.admin.domain.exception.NameAlreadyExistsInDatabaseException;
import org.silverpeas.core.util.StringUtil;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

public abstract class AbstractDomainService implements DomainService {

  /**
   * Check domain's name validity (no white spaces, only alphanumeric characters, no duplicate in
   * database)
   * @param domainName the new domain name
   * @throws NameAlreadyExistsInDatabaseException if domain name is not valid
   * @throws AdminException if a technical problem occurs during checks
   */
  protected void checkDomainName(String domainName)
      throws AdminException, NameAlreadyExistsInDatabaseException {

    // Check domain name availability in database
    Domain[] tabDomain = getAdminService().getAllDomains();
    for (Domain domain : tabDomain) {
      if (domain.getName().equalsIgnoreCase(domainName)) {
        throw new NameAlreadyExistsInDatabaseException(domainName);
      }
    }
  }

  /**
   * Get the next domain id
   * @return new domain id
   * @throws DomainCreationException
   */
  protected String getNextDomainId() throws DomainCreationException {
    SilverTrace
        .info("admin", "AbstractDomainService.getNextDomainId()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getNextDomainId();
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
   * @throws DomainCreationException
   */
  protected String registerDomain(Domain domainToCreate) throws DomainCreationException {
    if (StringUtil.isNotDefined(domainToCreate.getId())) {
      domainToCreate.setId("-1");
    }
    try {
      return getAdminService().addDomain(domainToCreate);
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
    try {
      return getAdminService().removeDomain(domainToRemove.getId());
    } catch (AdminException e) {
      throw new DomainDeletionException("AbstractDomainService.unRegisterDomain()", e);
    }
  }
}
