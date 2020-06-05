/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.viewer.model.DocumentView;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.viewer.model.ViewerSettings.getLicenceKey;

/**
 * The document view entity is a document view instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentViewEntity extends AbstractPreviewEntity<DocumentViewEntity> {
  private static final long serialVersionUID = 4270519541076741138L;

  @XmlElement(defaultValue = "")
  private String documentId;

  @XmlElement(defaultValue = "")
  private String originalFileName;

  @XmlElement(defaultValue = "")
  private String width;

  @XmlElement(defaultValue = "")
  private String height;

  @XmlElement(defaultValue = "")
  private String language;

  @XmlElement(defaultValue = "")
  private String viewerUri;

  @XmlElement(defaultValue = "")
  private String viewMode = "Default";

  /**
   * Creates a new document view entity from the specified document view.
   * @param documentView the {@link DocumentView} data.
   * @return the entity representing the specified document view.
   */
  public static DocumentViewEntity createFrom(final DocumentView documentView) {
    return new DocumentViewEntity(documentView);
  }

  /**
   * Default constructor
   * @param documentView the {@link DocumentView} data.
   *
   */
  private DocumentViewEntity(final DocumentView documentView) {
    documentId = documentView.getDocumentId();
    language = documentView.getLanguage();
    originalFileName = documentView.getOriginalFileName();
    width = documentView.getWidth();
    height = documentView.getHeight();
    this.viewerUri = getApplicationURL() + "/services/media/viewer/embed/";
    if (documentView.getServerFilePath().endsWith("file.pdf")) {
      viewerUri += "pdf";
    } else {
      if (isDefined(getLicenceKey())) {
        viewMode = "Zine";
      }
      viewerUri += "fp";
    }
    viewerUri += "?documentId=" + documentId + "&language=" + language;
  }

  protected DocumentViewEntity() {
  }

  /**
   * @return the documentId
   */
  protected String getDocumentId() {
    return documentId;
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

  /**
   * @return the language
   */
  protected String getLanguage() {
    return language;
  }

  public String getViewerUri() {
    return viewerUri;
  }

  public String getViewMode() {
    return viewMode;
  }
}
