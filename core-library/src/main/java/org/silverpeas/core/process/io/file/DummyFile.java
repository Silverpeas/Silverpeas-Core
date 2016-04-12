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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.process.io.file;

import org.silverpeas.core.util.file.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Simple dummy representation of a File that has to be handle.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 17/10/13
 */
public class DummyFile extends AbstractDummyHandledFile {

  private final String componentInstanceId;
  private final File file;
  private final boolean deleted;

  /**
   * Case of the file is considered as a creation.
   * @param file
   * @param componentInstanceId
   */
  public DummyFile(final File file, final String componentInstanceId) {
    this(file, componentInstanceId, false);
  }

  /**
   * Default constructor.
   * @param file
   * @param deleted
   */
  public DummyFile(final File file, final String componentInstanceId, final boolean deleted) {
    this.file = file;
    this.componentInstanceId = componentInstanceId;
    this.deleted = deleted;
  }

  @Override
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  @Override
  public String getPath() {
    return file.getPath();
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public long getSize() {
    if (!file.exists()) {
      return 0;
    }
    return FileUtils.sizeOf(file);
  }

  @Override
  public String getMimeType() {
    return FileUtil.getMimeType(getPath());
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }
}
