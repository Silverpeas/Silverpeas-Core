/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.io.media.image.thumbnail.model;

import java.io.Serializable;

import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ResourceLocator;

/**
 * Class declaration
 * @author
 */
public class ThumbnailDetail implements Serializable, MimeTypes {

  private static final SettingBundle publicationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.publication.publicationSettings");
  public static final int THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE = 1;

  private static final long serialVersionUID = 1L;

  private int objectId;
  private int objectType;

  private String instanceId = null;
  private String originalFileName = null;
  private String cropFileName = null;
  private int xStart = -1;
  private int yStart = -1;
  private int xLength = -1;
  private int yLength = -1;

  private String mimeType = null;

  public ThumbnailDetail(String instanceId, int objectId, int objectType) {
    this.instanceId = instanceId;
    this.objectId = objectId;
    this.objectType = objectType;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public String getCropFileName() {
    return cropFileName;
  }

  public String getInstanceId() {
    return instanceId;
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
    this.objectId = objectId;
  }

  public int getObjectId() {
    return objectId;
  }

  public int getObjectType() {
    return objectType;
  }

  public void setObjectType(int objectType) {
    this.objectType = objectType;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
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

  public String getMimeType() {
    return mimeType;
  }

  public boolean isCropable() {
    return StringUtil.isDefined(getOriginalFileName()) && !getOriginalFileName().startsWith("/");
  }

  public String getURL() {
    String image = getOriginalFileName();
    if (image.startsWith("/")) {
      // case of an image from 'gallery' app
      return image; // + "&Size=133x100";
    }
    if (getCropFileName() != null) {
      image = getCropFileName();
    }
    return FileServerUtils.getUrl(getInstanceId(), "thumbnail", image, getMimeType(),
        publicationSettings.getString("imagesSubDirectory"));
  }

}