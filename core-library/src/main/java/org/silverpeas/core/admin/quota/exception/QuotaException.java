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
package org.silverpeas.core.admin.quota.exception;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.quota.model.Quota;


/**
 * @author Yohann Chastagnier
 */
public class QuotaException extends SilverpeasException {
  private static final long serialVersionUID = -822107677650523574L;

  private final Quota quota;

  public QuotaException(final Quota quota, final String message, final String... parameters) {
    super(message, parameters);
    this.quota = quota;
  }

  public QuotaException(final Quota quota, final String message, final Throwable cause) {
    super(message, cause);
    this.quota = quota;
  }

  public QuotaException(final Quota quota, final Throwable cause) {
    super(cause);
    this.quota = quota;
  }

  /**
   * @return the quota concerned by the thrown of this exception.
   */
  public Quota getQuota() {
    return quota;
  }
}
