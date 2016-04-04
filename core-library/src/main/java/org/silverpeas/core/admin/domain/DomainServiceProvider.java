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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.quota.service.QuotaService;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A provider of DomainService objects. Its aim is to manage the life-cycle of such objects and so
 * to encapsulates from the DomainService client the adopted policy about that life-cycle.
 */
@Singleton
public class DomainServiceProvider {

  @Inject
  @Named("externalDomainService")
  private DomainService externalDomainService;

  @Inject
  @Named("sqlDomainService")
  private DomainService sqlDomainService;

  @Inject
  private QuotaService<UserDomainQuotaKey> userDomainQuotaService;

  /**
   * Gets an instance of this DomainServiceFactory class.
   * @return a DomainServiceFactory instance.
   */
  private static DomainServiceProvider getProvider() {
    return ServiceProvider.getService(DomainServiceProvider.class);
  }

  /**
   * Gets a DomainService instance.
   * @return a DomainService instance.
   */
  public static DomainService getDomainService(final DomainType type) {
    switch (type) {
      case EXTERNAL:
        return getProvider().externalDomainService;
      case SQL:
        return getProvider().sqlDomainService;
      default:
        SilverTrace.error("admin", getProvider().getClass().getSimpleName() + ".getDomainService()",
            "EX_NO_MESSAGES", "Only SQL and SILVERPEAS Domain Services are implemented");
        return null;
    }
  }

  /**
   * Gets the QuotaService instance associated to the user in domain quota.
   * @return the QuotaService associated to the user in domain quota.
   */
  public static QuotaService<UserDomainQuotaKey> getUserDomainQuotaService() {
    return getProvider().userDomainQuotaService;
  }

  private DomainServiceProvider() {
  }
}
