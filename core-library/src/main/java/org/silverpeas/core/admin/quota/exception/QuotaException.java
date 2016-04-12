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
package org.silverpeas.core.admin.quota.exception;

import org.silverpeas.core.admin.quota.model.Quota;

import org.silverpeas.core.exception.SilverpeasException;

/**
 * @author Yohann Chastagnier
 */
public class QuotaException extends SilverpeasException {
  private static final long serialVersionUID = -822107677650523574L;

  private final Quota quota;

  /**
   * Default constructor
   * @param quota
   * @param messageSuffix
   */
  public QuotaException(final Quota quota, final String messageSuffix) {
    this(quota, messageSuffix, null);
  }

  /**
   * Default constructor
   * @param quota
   * @param messageSuffix
   * @param exception
   */
  public QuotaException(final Quota quota, final String messageSuffix, final Exception exception) {
    super("AbstractQuotaService", SilverpeasException.ERROR, "quota." + messageSuffix,
        "quotaType=" + quota.getType() + ", resourceId=" + quota.getResourceId() + ", minCount=" +
            quota.getMinCount() + ", maxCount=" + quota.getMaxCount() + ", count=" +
            quota.getCount(), exception);
    this.quota = quota;
  }

  /**
   * Default constructor
   * @param quota
   * @param messageSuffix
   */
  public QuotaException(final Exception exception) {
    super("NoClass", SilverpeasException.ERROR, "", exception);
    quota = null;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.exception.SilverpeasException#getModule()
   */
  @Override
  public String getModule() {
    return "quota";
  }

  /**
   * @return the quota
   */
  public Quota getQuota() {
    return quota;
  }
}
