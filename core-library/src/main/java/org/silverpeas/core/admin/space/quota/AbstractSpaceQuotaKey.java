/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.space.quota;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.quota.QuotaKey;
import org.silverpeas.core.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
abstract public class AbstractSpaceQuotaKey implements QuotaKey {

  private final SpaceInst space;

  /**
   * Builds the space quota key from a given SpaceInst
   * @param space
   */
  protected AbstractSpaceQuotaKey(final SpaceInst space) {
    this.space = space;
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#isValid()
   */
  @Override
  public boolean isValid() {
    return (space != null && StringUtil.isDefined(space.getId()));
  }

  /*
   * (non-Javadoc)
   * @see QuotaKey#getResourceId()
   */
  @Override
  public String getResourceId() {
    return String.valueOf(space.getLocalId());
  }

  /**
   * @return the space
   */
  public SpaceInst getSpace() {
    return space;
  }
}
