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
package org.silverpeas.core.webapi.viewer;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.silverpeas.core.viewer.model.Preview;

/**
 * The preview entity is a preview instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PreviewEntity extends AbstractPreviewEntity<PreviewEntity> {
  private static final long serialVersionUID = 4270519541076741138L;

  @XmlElement(defaultValue = "")
  private String url;

  @XmlElement(defaultValue = "")
  private String originalFileName;

  @XmlElement(defaultValue = "")
  private String width;

  @XmlElement(defaultValue = "")
  private String height;

  /**
   * Creates a new Preview entity from the specified preview.
   * @param request the current http request
   * @param preview the preview to entitify.
   * @return the entity representing the specified preview.
   */
  public static PreviewEntity createFrom(final HttpServletRequest request, final Preview preview) {
    return new PreviewEntity(request, preview);
  }

  /**
   * Default constructorC
   * @param request
   * @param preview
   */
  protected PreviewEntity(final HttpServletRequest request, final Preview preview) {
    url = preview.getURLAsString().replaceAll("[/]{2,}", "/");
    originalFileName = preview.getOriginalFileName();
    width = preview.getWidth();
    height = preview.getHeight();
  }

  protected PreviewEntity() {
  }

  /**
   * @return the url
   */
  protected String getURL() {
    return url;
  }

  /**
   * @return the originalFileName
   */
  protected String getOriginalFileName() {
    return originalFileName;
  }

  /**
   * @return the width
   */
  protected String getWidth() {
    return width;
  }

  /**
   * @return the height
   */
  protected String getHeight() {
    return height;
  }
}
