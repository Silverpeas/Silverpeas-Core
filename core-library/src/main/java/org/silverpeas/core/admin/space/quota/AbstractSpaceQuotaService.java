/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.core.admin.quota.service.AbstractQuotaService;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.logging.SilverLogger;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractSpaceQuotaService<T extends AbstractSpaceQuotaKey>
    extends AbstractQuotaService<T> implements SpaceQuotaService<T> {

  /**
   * Creates a quota key
   * @param space
   * @return
   */
  protected abstract T createKeyFrom(SpaceInst space);

  /*
     * (non-Javadoc)
     *
     * org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaService#getQuotaReachedFromSpacePath(org.
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
      } catch (final AdminException | QuotaException e) {
        SilverLogger.getLogger(this).error(e);
      }

      if (!spaceQuotaReached.isReached()) {
        spaceQuotaReached = new Quota();
      }
    }
    return spaceQuotaReached;
  }

  @Override
  public Quota verify(final T key, final Quota quota,
      final AbstractQuotaCountingOffset countingOffset) throws QuotaException {
    if (!isActivated()) {
      return new Quota();
    }
    T currentKey = key;
    Quota verifiedQuota = verifyQuota(quota, countingOffset);
    while (currentKey.isValid() && !currentKey.getSpace().isRoot()) {
      currentKey = createKeyFrom(OrganizationControllerProvider.getOrganisationController()
          .getSpaceInstById(currentKey.getSpace().getDomainFatherId()));
      verifiedQuota = verifyQuota(get(currentKey), countingOffset);
    }
    return verifiedQuota;
  }
}
