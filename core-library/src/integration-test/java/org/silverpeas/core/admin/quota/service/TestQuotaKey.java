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
package org.silverpeas.core.admin.quota.service;

import org.silverpeas.core.admin.quota.constant.QuotaType;
import org.silverpeas.core.admin.quota.QuotaKey;

/**
 * @author Yohann Chastagnier
 */
public class TestQuotaKey implements QuotaKey {

  final String resourceId;

  public TestQuotaKey(final String resourceId) {
    this.resourceId = resourceId;
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }

  /**
   * @return the resourceId
   */
  @Override
  public String getResourceId() {
    return resourceId;
  }

  @Override
  public QuotaType getQuotaType() {
    return QuotaType.USERS_IN_DOMAIN;
  }
}
