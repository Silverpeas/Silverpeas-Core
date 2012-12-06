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

import static com.stratelia.webactiv.util.FileRepositoryManager.getAbsolutePath;
import static org.apache.commons.io.FileUtils.sizeOfDirectory;

import java.io.File;

import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;

import com.silverpeas.annotation.Service;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultDataStorageSpaceQuotaService extends
    AbstractSpaceQuotaService<DataStorageSpaceQuotaKey> implements DataStorageSpaceQuotaService {
  private static long dataStorageInPersonalSpaceQuotaDefaultMaxCount;
  static {
    final ResourceLocator settings =
        new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings", "");
    dataStorageInPersonalSpaceQuotaDefaultMaxCount =
        settings.getLong("quota.personalspace.datastorage.default.maxCount", 0);
    if (dataStorageInPersonalSpaceQuotaDefaultMaxCount < 0) {
      dataStorageInPersonalSpaceQuotaDefaultMaxCount = 0;
    }
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
      for (final ComponentInst component : key.getSpace().getAllComponentsInst()) {
        file = new File(getAbsolutePath(component.getId()));
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
    if (key.isValid()) {
      if (key.getSpace().isPersonalSpace()) {
        // Setting a dummy id
        quota.setId(-1L);
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
}
