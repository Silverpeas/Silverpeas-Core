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
package org.silverpeas.admin.space.quota;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.quota.service.AbstractQuotaService;
import org.silverpeas.util.UnitUtil;

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractSpaceQuotaService<T extends AbstractSpaceQuotaKey>
    extends AbstractQuotaService<T> implements SpaceQuotaService<T> {

  protected static long dataStorageInPersonalSpaceQuotaDefaultMaxCount;

  static {
    final ResourceLocator settings =
        new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings",
            "");
    dataStorageInPersonalSpaceQuotaDefaultMaxCount =
        settings.getLong("quota.personalspace.datastorage.default.maxCount", 0);
    if (dataStorageInPersonalSpaceQuotaDefaultMaxCount < 0) {
      dataStorageInPersonalSpaceQuotaDefaultMaxCount = 0;
    }
    dataStorageInPersonalSpaceQuotaDefaultMaxCount = UnitUtil
        .convertTo(dataStorageInPersonalSpaceQuotaDefaultMaxCount, UnitUtil.memUnit.MB,
            UnitUtil.memUnit.B);
  }

  /**
   * Creates a quota key
   * @param space
   * @return
   */
  abstract protected T createKeyFrom(SpaceInst space);

  @Override
  public Quota get(final T key) throws QuotaException {
    if (key.getSpace().isPersonalSpace()) {
      Quota quota = new Quota();
      // Setting a dummy id
      quota.setId(-1L);
      // The type
      quota.setType(key.getQuotaType());
      // The resource id
      quota.setResourceId(key.getResourceId());
      // Setting the max count
      quota.setMaxCount(dataStorageInPersonalSpaceQuotaDefaultMaxCount);
      // Current count
      quota.setCount(getCurrentCount(key));
      // Returning the dummy quota of the personal space
      return quota;
    }
    return super.get(key);
  }

  /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.admin.space.quota.ComponentSpaceQuotaService#getQuotaReachedFromSpacePath(org.
     * silverpeas.admin.space.quota.ComponentSpaceQuotaKey)
     */
  @Override
  public Quota getQuotaReachedFromSpacePath(final T key) {
    Quota spaceQuotaReached = new Quota();
    if (key.isValid()) {
      try {
        spaceQuotaReached = get(key);
        SpaceInst fromSpaceTheQuotaIsReached =
            getAdminService().getSpaceInstById(key.getResourceId());

        while (!spaceQuotaReached.isReached() && !fromSpaceTheQuotaIsReached.isRoot()) {

          // Parent space
          fromSpaceTheQuotaIsReached =
              getAdminService().getSpaceInstById(fromSpaceTheQuotaIsReached.getDomainFatherId());

          // Parent quota
          spaceQuotaReached = get(createKeyFrom(fromSpaceTheQuotaIsReached));
        }
      } catch (final AdminException e) {
        SilverTrace.error("quota", "AbstractSpaceQuotaService.getQuotaReachedFromSpacePath",
            "quota.MSG_ERR_GET_SPACE", e);
      } catch (final QuotaException qe) {
        SilverTrace.error("quota", "AbstractSpaceQuotaService.getQuotaReachedFromSpacePath",
            "quota.EX_CANT_GET_SPACE_QUOTA", qe);
      }

      if (!spaceQuotaReached.isReached()) {
        spaceQuotaReached = new Quota();
      }
    }
    return spaceQuotaReached;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.AbstractQuotaService#verify(org.silverpeas.quota.QuotaKey,
   * org.silverpeas.quota.offset.AbstractQuotaCountingOffset)
   */
  @Override
  public Quota verify(T key, final AbstractQuotaCountingOffset countingOffset)
      throws QuotaException {
    Quota quota = super.verify(key, countingOffset);
    while (key.isValid() && !key.getSpace().isRoot()) {
      key = createKeyFrom(OrganisationControllerFactory.getOrganisationController()
          .getSpaceInstById(key.getSpace().getDomainFatherId()));
      quota = super.verify(key, countingOffset);
    }
    return quota;
  }
}
