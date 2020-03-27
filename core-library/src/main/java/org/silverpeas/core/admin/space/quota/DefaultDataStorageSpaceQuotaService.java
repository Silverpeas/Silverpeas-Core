/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.file.FileRepositoryManager.getAbsolutePath;
import static org.silverpeas.core.util.file.FileRepositoryManager.getDirectorySize;

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
    if (!isActivated()) {
      return new Quota();
    }
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
   *
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
  public long getCurrentCount(final DataStorageSpaceQuotaKey key) {
    long currentCount = 0;
    // space could be null if user space is performed
    if (key.getSpace() != null) {
      final List<File> files = Stream
          .of(OrganizationController.get().getAllComponentIdsRecur(key.getSpace().getId()))
          .map(i -> new File(getAbsolutePath(i)))
          .filter(File::exists)
          .collect(Collectors.toList());
      currentCount = getDirectorySize(files);
    }
    return currentCount;
  }

  @Override
  protected boolean isActivated() {
    return settings.getBoolean("quota.space.datastorage.activated", false);
  }
}
