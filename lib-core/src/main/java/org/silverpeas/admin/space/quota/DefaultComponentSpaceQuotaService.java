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

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;
import static org.silverpeas.admin.space.quota.ComponentSpaceQuotaKey.from;

import org.silverpeas.admin.space.SpaceServiceFactory;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.service.AbstractQuotaService;

import com.silverpeas.annotation.Service;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultComponentSpaceQuotaService extends AbstractQuotaService<ComponentSpaceQuotaKey>
    implements ComponentSpaceQuotaService {

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.admin.space.quota.ComponentSpaceQuotaService#getQuotaReachedFromSpacePath(org.
   * silverpeas.admin.space.quota.ComponentSpaceQuotaKey)
   */
  @Override
  public Quota getQuotaReachedFromSpacePath(final ComponentSpaceQuotaKey key) {
    Quota componentSpaceQuotaReached = new Quota();
    if (key.isValid()) {
      try {
        componentSpaceQuotaReached = get(key);
        SpaceInstLight fromSpaceComponentSpaceQuotaReached =
            getAdminService().getSpaceInstLightById(key.getResourceId());

        while (!componentSpaceQuotaReached.isReached() &&
            !fromSpaceComponentSpaceQuotaReached.isRoot()) {

          // Parent space
          fromSpaceComponentSpaceQuotaReached =
              getAdminService().getSpaceInstLightById(
                  fromSpaceComponentSpaceQuotaReached.getFatherId());

          // Parent quota
          componentSpaceQuotaReached =
              SpaceServiceFactory.getComponentSpaceQuotaService().get(
                  ComponentSpaceQuotaKey.from(fromSpaceComponentSpaceQuotaReached));
        }
      } catch (final AdminException e) {
        SilverTrace.error("quota", "ComponentSpaceQuotaService.getQuotaReachedRecursively",
            "quota.MSG_ERR_GET_SPACE", e);
      } catch (final QuotaException qe) {
        SilverTrace.error("quota", "ComponentSpaceQuotaService.getQuotaReachedRecursively",
            "quota.EX_CANT_GET_COMPONENT_SPACE_QUOTA", qe);
      }

      if (!componentSpaceQuotaReached.isReached()) {
        componentSpaceQuotaReached = new Quota();
      }
    }
    return componentSpaceQuotaReached;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.AbstractQuotaService#verify(org.silverpeas.quota.QuotaKey)
   */
  @Override
  public Quota verify(ComponentSpaceQuotaKey key) throws QuotaException {
    Quota quota = super.verify(key);
    while (key.isValid() && !key.getSpace().isRoot()) {
      key =
          from(OrganizationControllerFactory.getFactory().getOrganizationController()
              .getSpaceInstById(key.getSpace().getDomainFatherId()));
      quota = super.verify(key);
    }
    return quota;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#getCurrentCount(org.silverpeas.quota.QuotaKey)
   */
  @Override
  public int getCurrentCount(final ComponentSpaceQuotaKey key) throws QuotaException {

    // Initializing the counting result
    int currentCount = 0;

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
