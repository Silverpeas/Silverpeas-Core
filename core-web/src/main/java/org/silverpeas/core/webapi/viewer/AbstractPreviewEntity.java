/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.web.rs.WebEntity;

import java.net.URI;

/**
 * The preview entity is a preview instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
public abstract class AbstractPreviewEntity<T extends AbstractPreviewEntity<T>> implements
    WebEntity {
  private static final long serialVersionUID = 4118811534281560380L;

  private URI uri;
  private String documentId;
  private String documentType;
  private String originalFileName;
  private String width;
  private String height;
  private String language;

  protected AbstractPreviewEntity() {
  }

  /**
   * Default constructor
   * @param preview the {@link Preview} data.
   *
   */
  protected AbstractPreviewEntity(final Preview preview) {
    documentId = preview.getDocumentId();
    documentType = preview.getDocumentType();
    language = preview.getLanguage();
    originalFileName = preview.getOriginalFileName();
    width = preview.getWidth();
    height = preview.getHeight();
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.WebEntity#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public String getDocumentId() {
    return documentId;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public String getWidth() {
    return width;
  }

  public String getHeight() {
    return height;
  }

  public String getLanguage() {
    return language;
  }
}
