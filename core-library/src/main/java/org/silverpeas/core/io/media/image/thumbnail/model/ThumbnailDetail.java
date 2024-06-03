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
package org.silverpeas.core.io.media.image.thumbnail.model;

import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Representation of a thumbnail of an object.
 */
public class ThumbnailDetail implements Thumbnail {

  private static final SettingBundle publicationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.publication.publicationSettings");
  public static final int THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE = 1;

  private static final long serialVersionUID = 1L;

  private final ThumbnailReference reference;
  private String originalFileName = null;
  private String cropFileName = null;
  private int xStart = -1;
  private int yStart = -1;
  private int xLength = -1;
  private int yLength = -1;

  private String mimeType = null;

  public ThumbnailDetail(String instanceId, int objectId, int objectType) {
    this.reference = new ThumbnailReference(objectId, instanceId, objectType);
  }

  @Override
  public ThumbnailReference getReference() {
    return reference;
  }

  @Override
  public boolean isCropped() {
    return StringUtil.isDefined(cropFileName);
  }

  @Override
  public String getOriginalFileName() {
    return originalFileName;
  }

  @Override
  public String getCropFileName() {
    return cropFileName;
  }

  /**
   * Returns the image file name of thumbnail by priority.
   * If the crop file name exists it is returned, otherwise the original file name is returned.
   * @return a file name as string.
   */
  @Override
  public String getImageFileName() {
    return this.getCropFileName() != null ? this.getCropFileName() : this.getOriginalFileName();
  }

  public String getInstanceId() {
    return reference.getComponentInstanceId();
  }

  public int getXStart() {
    return xStart;
  }

  public int getYStart() {
    return yStart;
  }

  public int getXLength() {
    return xLength;
  }

  public int getYLength() {
    return yLength;
  }

  public void setObjectId(int objectId) {
    this.reference.setObjectId(objectId);
  }

  public int getObjectId() {
    return this.reference.getObjectId();
  }

  public int getObjectType() {
    return this.reference.getObjectType();
  }

  public void setObjectType(int objectType) {
    this.reference.setObjectType(objectType);
  }

  public void setInstanceId(String instanceId) {
    this.reference.setComponentName(instanceId);
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public void setCropFileName(String cropFileName) {
    this.cropFileName = cropFileName;
  }

  public void setXStart(int xStart) {
    this.xStart = xStart;
  }

  public void setYStart(int yStart) {
    this.yStart = yStart;
  }

  public void setXLength(int xLength) {
    this.xLength = xLength;
  }

  public void setYLength(int yLength) {
    this.yLength = yLength;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public boolean canBeCropped() {
    return StringUtil.isDefined(getOriginalFileName()) && !getOriginalFileName().startsWith("/");
  }

  @Override
  public String getURL() {
    String image = getOriginalFileName();
    if (image.startsWith("/")) {
      // case of an image from 'gallery' app
      return image;
    }
    if (getCropFileName() != null) {
      image = getCropFileName();
    }
    return FileServerUtils.getUrl(getInstanceId(), "thumbnail", image, getMimeType(),
        publicationSettings.getString("imagesSubDirectory"));
  }

  @Override
  public Optional<Path> getPath() {
    String image;
    if (getCropFileName() != null) {
      image = getCropFileName();
    } else {
      image = getOriginalFileName();
    }
    if (image.startsWith("/")) {
      return Optional.empty();
    }
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId()) +
        publicationSettings.getString("imagesSubDirectory");
    return Optional.of(Path.of(directory, image));
  }
}