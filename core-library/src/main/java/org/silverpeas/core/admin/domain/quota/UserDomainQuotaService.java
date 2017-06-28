/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.admin.domain.quota;

import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.service.AbstractQuotaService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class UserDomainQuotaService extends AbstractQuotaService<UserDomainQuotaKey> {

  @Inject
  private DomainDriverManager domainDriverManager;

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");

  /*
   * (non-Javadoc)
   * @see QuotaService#getCurrentCount(QuotaKey)
   */
  @Override
  @Transactional
  public long getCurrentCount(final UserDomainQuotaKey key) throws QuotaException {
    try {
      return domainDriverManager.getAllUsers(key.getResourceId()).length;
    } catch (final Exception e) {
      throw new QuotaException(e);
    }
  }

  @Override
  protected boolean isActivated() {
    return settings.getBoolean("quota.domain.users.activated", false);
  }
}
