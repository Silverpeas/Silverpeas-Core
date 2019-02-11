/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.exception.DomainConflictException;
import org.silverpeas.core.admin.domain.exception.DomainCreationException;
import org.silverpeas.core.admin.domain.exception.DomainDeletionException;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("externalDomainService")
public class ExternalDomainService extends AbstractDomainService {

  @Override
  public String createDomain(Domain domainToCreate) throws DomainCreationException,
      DomainConflictException {
    // Check domain name
    try {
      checkDomainName(domainToCreate.getName());
    } catch (AdminException e) {
      throw new DomainCreationException(domainToCreate.toString(), e);
    }

    String id;
    domainToCreate.setId("-1");
    id = registerDomain(domainToCreate);
    if (!StringUtil.isDefined(id)) {
      throw new DomainCreationException(domainToCreate.toString());
    }

    return id;
  }

  @Override
  public String deleteDomain(Domain domainToRemove) throws DomainDeletionException {
    String id;
    id = unRegisterDomain(domainToRemove);
    if (!StringUtil.isDefined(id)) {
      throw new DomainDeletionException(domainToRemove.toString());
    }

    return id;
  }

}
