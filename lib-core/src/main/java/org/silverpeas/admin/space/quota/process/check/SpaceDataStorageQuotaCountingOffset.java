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
package org.silverpeas.admin.space.quota.process.check;

import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.quota.offset.AbstractQuotaCountingOffset;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;

/**
 * @author Yohann Chastagnier
 */
public class SpaceDataStorageQuotaCountingOffset extends AbstractQuotaCountingOffset {

  private final SpaceInst space;
  private final FileHandler fileHandler;

  /**
   * Gets an instance from a SpaceInst and a FileHandler
   * @param space
   * @param fileHandler
   * @return
   */
  public static SpaceDataStorageQuotaCountingOffset from(final SpaceInst space,
      final FileHandler fileHandler) {
    return new SpaceDataStorageQuotaCountingOffset(space, fileHandler);
  }

  /**
   * Default hidden constructor
   * @param space
   * @param fileHandler
   */
  private SpaceDataStorageQuotaCountingOffset(final SpaceInst space, final FileHandler fileHandler) {
    this.space = space;
    this.fileHandler = fileHandler;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.quota.offset.AbstractQuotaCountingOffset#getOffset()
   */
  @Override
  public long getOffset() {

    // Initializing the counting result
    long currentCountOffset = 0;

    // space could be null if user space is performed
    if (space != null) {
      for (final ComponentInst component : space.getAllComponentsInst()) {
        currentCountOffset += fileHandler.sizeOfSessionWorkingPath(component.getId());
      }
    }

    // Result
    return currentCountOffset;
  }
}
