/*
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
package org.silverpeas.admin.space.quota;

import org.silverpeas.quota.exception.QuotaException;

import com.silverpeas.annotation.Service;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.SpaceInst;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultComponentSpaceQuotaService extends
    AbstractSpaceQuotaService<ComponentSpaceQuotaKey> implements ComponentSpaceQuotaService {

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.admin.space.quota.AbstractSpaceQuotaService#createKeyFrom(com.stratelia.webactiv
   * .beans.admin.SpaceInst)
   */
  @Override
  protected ComponentSpaceQuotaKey createKeyFrom(final SpaceInst space) {
    return ComponentSpaceQuotaKey.from(space);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#getCurrentCount(org.silverpeas.quota.QuotaKey)
   */
  @Override
  public long getCurrentCount(final ComponentSpaceQuotaKey key) throws QuotaException {

    // Initializing the counting result
    long currentCount = 0;

    // space could be null if user space is performed
    if (key.getSpace() != null) {
      currentCount =
          OrganizationControllerFactory.getFactory().getOrganizationController()
              .getAllComponentIdsRecur(key.getResourceId()).length;
    }

    // Result
    return currentCount;
  }
}
