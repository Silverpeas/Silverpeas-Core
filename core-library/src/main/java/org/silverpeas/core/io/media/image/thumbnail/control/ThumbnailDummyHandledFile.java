/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.image.thumbnail.control;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.process.io.file.AbstractDummyHandledFile;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public class ThumbnailDummyHandledFile extends AbstractDummyHandledFile {

  private final Thumbnail thumbnail;
  private final File thumbnailFile;
  private final boolean deleted;
  private ResourceReference targetPK = null;

  public ThumbnailDummyHandledFile(final Thumbnail thumbnail, final File thumbnailFile) {
    this(thumbnail, thumbnailFile, false);
  }

  public ThumbnailDummyHandledFile(final Thumbnail thumbnail, final File thumbnailFile,
      final ResourceReference targetPK) {
    this(thumbnail, thumbnailFile);
    this.targetPK = targetPK;
  }

  public ThumbnailDummyHandledFile(final Thumbnail thumbnail, final File thumbnailFile,
      final boolean deleted) {
    this.thumbnail = thumbnail;
    this.thumbnailFile = thumbnailFile;
    this.deleted = deleted;
  }

  @Override
  public String getComponentInstanceId() {
    if (targetPK != null) {
      return targetPK.getInstanceId();
    }
    return thumbnail.getReference().getComponentInstanceId();
  }

  @Override
  public String getPath() {
    return thumbnailFile.getPath();
  }

  @Override
  public String getName() {
    return thumbnailFile.getName();
  }

  @Override
  public long getSize() {
    if (!thumbnailFile.exists()) {
      return 0;
    }
    return FileUtils.sizeOf(thumbnailFile);
  }

  @Override
  public String getMimeType() {
    if (thumbnailFile.exists() && thumbnailFile.isFile()) {
      return FileUtil.getMimeType(thumbnailFile.getPath());
    }
    return thumbnail.getMimeType();
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
