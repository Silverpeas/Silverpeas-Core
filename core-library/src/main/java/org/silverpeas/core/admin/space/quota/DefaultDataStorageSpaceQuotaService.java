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
package org.silverpeas.core.admin.space.quota;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.quota.offset.AbstractQuotaCountingOffset;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Singleton;
import java.io.File;

import static org.apache.commons.io.FileUtils.sizeOfDirectory;
import static org.silverpeas.core.util.file.FileRepositoryManager.getAbsolutePath;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultDataStorageSpaceQuotaService
    extends AbstractSpaceQuotaService<DataStorageSpaceQuotaKey>
    implements DataStorageSpaceQuotaService {

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");

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
      quota.setQuotaId(-1L);
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
   * org.silverpeas.core.admin.space.quota.AbstractSpaceQuotaService#createKeyFrom(com.stratelia.webactiv
   * .beans.admin.SpaceInst)
   */
  @Override
  protected DataStorageSpaceQuotaKey createKeyFrom(final SpaceInst space) {
    return DataStorageSpaceQuotaKey.from(space);
  }

  /*
   * (non-Javadoc)
   * @see QuotaService#getCurrentCount(QuotaKey)
   */
  @Override
  public long getCurrentCount(final DataStorageSpaceQuotaKey key) throws QuotaException {

    // Initializing the counting result
    long currentCount = 0;

    // space could be null if user space is performed
    if (key.getSpace() != null) {
      File file;
      for (final String componentId : OrganizationControllerProvider.getOrganisationController()
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
   * org.silverpeas.core.admin.space.quota.AbstractSpaceQuotaService#verify(org.silverpeas.core.admin.space
   * .quota.AbstractSpaceQuotaKey, AbstractQuotaCountingOffset)
   */
  @Override
  public Quota verify(final DataStorageSpaceQuotaKey key,
      final AbstractQuotaCountingOffset countingOffset) throws QuotaException {
    Quota quota = new Quota();
    if (isActivated() && key.isValid()) {
      if (key.getSpace().isPersonalSpace()) {
        // Setting a dummy id
        quota.setQuotaId(-1L);
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
