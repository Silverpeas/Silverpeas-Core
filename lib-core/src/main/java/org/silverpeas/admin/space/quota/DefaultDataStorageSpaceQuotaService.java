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

import com.silverpeas.annotation.Service;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.util.UnitUtil;
import org.silverpeas.util.memory.MemoryUnit;

import java.io.File;

import static org.silverpeas.util.FileRepositoryManager.getAbsolutePath;
import static org.apache.commons.io.FileUtils.sizeOfDirectory;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultDataStorageSpaceQuotaService
    extends AbstractSpaceQuotaService<DataStorageSpaceQuotaKey>
    implements DataStorageSpaceQuotaService {

  private static final ResourceLocator settings =
      new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings", "");

  private static long dataStorageInPersonalSpaceQuotaDefaultMaxCount;

  static {
    dataStorageInPersonalSpaceQuotaDefaultMaxCount =
        settings.getLong("quota.personalspace.datastorage.default.maxCount", 0);
    if (dataStorageInPersonalSpaceQuotaDefaultMaxCount < 0) {
      dataStorageInPersonalSpaceQuotaDefaultMaxCount = 0;
    }
    dataStorageInPersonalSpaceQuotaDefaultMaxCount = UnitUtil
        .convertTo(dataStorageInPersonalSpaceQuotaDefaultMaxCount, MemoryUnit.MB,
            MemoryUnit.B);
  }

  @Override
  public Quota get(final DataStorageSpaceQuotaKey key) throws QuotaException {
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
   * org.silverpeas.admin.space.quota.AbstractSpaceQuotaService#createKeyFrom(com.stratelia.webactiv
   * .beans.admin.SpaceInst)
   */
  @Override
  protected DataStorageSpaceQuotaKey createKeyFrom(final SpaceInst space) {
    return DataStorageSpaceQuotaKey.from(space);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.service.QuotaService#getCurrentCount(org.silverpeas.quota.QuotaKey)
   */
  @Override
  public long getCurrentCount(final DataStorageSpaceQuotaKey key) throws QuotaException {

    // Initializing the counting result
    long currentCount = 0;

    // space could be null if user space is performed
    if (key.getSpace() != null) {
      File file;
      for (final String componentId : OrganisationControllerFactory.getOrganisationController()
          .getAllComponentIdsRecur(key.getSpace().getId())) {
        file = new File(getAbsolutePath(componentId));
        if (file.exists()) {
          currentCount += sizeOfDirectory(file);
        }
      }
    }

    // Result
    return currentCount;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.admin.space.quota.AbstractSpaceQuotaService#verify(org.silverpeas.admin.space
   * .quota.AbstractSpaceQuotaKey, org.silverpeas.quota.offset.AbstractQuotaCountingOffset)
   */
  @Override
  public Quota verify(final DataStorageSpaceQuotaKey key,
      final AbstractQuotaCountingOffset countingOffset) throws QuotaException {
    Quota quota = new Quota();
    if (isActivated() && key.isValid()) {
      if (key.getSpace().isPersonalSpace()) {
        // Setting a dummy id
        quota.setId(-1L);
        // The type
        quota.setType(key.getQuotaType());
        // The resource id
        quota.setResourceId(key.getResourceId());
        // Setting the max count
        quota.setMaxCount(dataStorageInPersonalSpaceQuotaDefaultMaxCount);
        // Verifying
        quota = super.verify(key, quota, countingOffset);
      } else {
        // Default verifying
        quota = super.verify(key, countingOffset);
      }
    }
    return quota;
  }

  @Override
  protected boolean isActivated() {
    return settings.getBoolean("quota.space.datastorage.activated", false);
  }
}
