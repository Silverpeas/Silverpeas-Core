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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.ResourceReference;

import java.io.Serializable;

/**
 * A thumbnail is an image summarizing a type of resource or the content of a contribution. Usually
 * such objects should implement the {@link WithThumbnail} interface. The thumbnail is an image
 * stored into a file on the filesystem and this image can be cropped either by the user or by
 * Silverpeas itself to answer some requirements.
 * @author mmoquillon
 */
public interface Thumbnail extends Serializable {

  /**
   * Gets the resource or the contribution in Silverpeas to which this thumbnail is related.
   * @return a reference to the resource or to the contribution related by this image.
   */
  ResourceReference getReference();

  /**
   * Gets the MIME type of the thumbnail. For example "image/jpeg" for a JPEG image.
   * @return the MIME type of the image.
   */
  String getMimeType();

  /**
   * Gets the name of the file in which is stored the thumbnail by following the process: first it
   * checks for a file with the cropped image of the thumbnail, if no such file is found, then it
   * rollbacks to the original file of the thumbnail.
   * If the crop file name exists it is returned, otherwise the original file name is returned.
   * @return a file name as string.
   */
  String getImageFileName();

  /**
   * Gets the name of the file in which is stored the image that was uploaded to be used as a
   * thumbnail of a resource in Silverpeas.
   * @return the original file name of this image.
   */
  String getOriginalFileName();

  /**
   * Gets the name of the file in which is stored the cropped version of the image and that will be
   * actually used as thumbnail. If no cropped version exists (checks with
   * the {@link Thumbnail#isCropped()} method), null is returned.
   * @return the file name of the cropped version of this image. Null if no cropped version exists.
   */
  String getCropFileName();

  /**
   * Is this thumbnail has been cropped?
   * @return true if it exists a cropped version of this thumbnail. False otherwise.
   */
  boolean isCropped();

  /**
   * Is this thumbnail can be cropped?
   * @return true if the Silverpeas can crop this image. False otherwise.
   */
  boolean canBeCropped();

  /**
   * Gets the URL of the thumbnail in Silverpeas in order to be rendered in the Web client.
   * <p>
   *   If {@link #isCropped()} returns true, then the URL returned is the one of cropped thumbnail.
   * </p>
   * @return the URL of this thumbnail.
   */
  String getURL();

  /**
   * Gets the URL of non cropped thumbnail in Silverpeas in order to be rendered in management
   * features.
   * @return the URL of the non cropped thumbnail.
   */
  String getNonCroppedURL();
}
