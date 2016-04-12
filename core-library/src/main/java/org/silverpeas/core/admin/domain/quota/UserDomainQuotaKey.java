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

import org.silverpeas.core.admin.quota.QuotaKey;
import org.silverpeas.core.admin.quota.constant.QuotaType;
import org.silverpeas.core.admin.quota.model.Quota;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class UserDomainQuotaKey implements QuotaKey {

  private final String resourceId;

  /**
   * Initializing a quota key from a given user
   * @param userDetail
   * @return
   */
  public static UserDomainQuotaKey from(final UserDetail userDetail) {
    return new UserDomainQuotaKey(userDetail);
  }

  /**
   * Initializing a quota key from a given quota
   * @param quota
   * @return
   */
  public static UserDomainQuotaKey from(final Quota quota) {
    return new UserDomainQuotaKey(quota);
  }

  /**
   * Initializing a quota key from a given domain
   * @param domain
   * @return
   */
  public static UserDomainQuotaKey from(final Domain domain) {
    return new UserDomainQuotaKey(domain);
  }

  /**
   * Builds the user domain quota key from a given UserDetail
   * @param userDetail
   */
  private UserDomainQuotaKey(final UserDetail userDetail) {
    resourceId = userDetail.getDomainId();
  }

  /**
   * Builds the user domain quota key from a given Quota
   * @param quota
   */
  private UserDomainQuotaKey(final Quota quota) {
    resourceId = quota.getResourceId();
  }

  /**
   * Builds the user domain quota key from a given Domain
   * @param domain
   */
  private UserDomainQuotaKey(final Domain domain) {
    resourceId = domain.getId();
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#isValid()
   */
  @Override
  public boolean isValid() {
    return StringUtil.isDefined(resourceId);
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#getQuotaType()
   */
  @Override
  public QuotaType getQuotaType() {
    return QuotaType.USERS_IN_DOMAIN;
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#getResourceId()
   */
  @Override
  public String getResourceId() {
    return resourceId;
  }
}
