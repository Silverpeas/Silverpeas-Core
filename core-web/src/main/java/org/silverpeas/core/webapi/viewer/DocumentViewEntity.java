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

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.viewer.model.DocumentView;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
  private String displayLicenseKey;

  @XmlElement(defaultValue = "")
  private String displayViewerPath;

  @XmlElement(defaultValue = "")
  private String url;

  @XmlElement(defaultValue = "")
  private String originalFileName;

  @XmlElement(defaultValue = "")
  private String width;

  @XmlElement(defaultValue = "")
  private String height;

  @XmlElement(defaultValue = "")
  private String language;

  @XmlElement(defaultValue = "")
  private int nbPages;

  @XmlElement(defaultValue = "")
  private boolean documentSplit;

  @XmlElement(defaultValue = "")
  private boolean searchDataComputed;

  /**
   * Creates a new document view entity from the specified document view.
   * @param documentView
   * @param language
   * @return the entity representing the specified document view.
   */
  public static DocumentViewEntity createFrom(final DocumentView documentView,
      final String language) {
    return new DocumentViewEntity(documentView, language);
  }

  /**
   * Default constructor
   * @param documentView
   * @param language
   */
  protected DocumentViewEntity(final DocumentView documentView, final String language) {
    displayLicenseKey = documentView.getDisplayLicenseKey();
    if (StringUtil.isDefined(displayLicenseKey)) {
      displayViewerPath = "/weblib/flexpaper/flash";
    } else {
      displayViewerPath = URLUtil.getApplicationURL() + "/util/flash/flexpaper";
    }
    url = documentView.getURLAsString();
    originalFileName = documentView.getOriginalFileName();
    width = documentView.getWidth();
    height = documentView.getHeight();
    this.language = language;
    nbPages = documentView.getNbPages();
    documentSplit = documentView.isDocumentSplit();
    searchDataComputed = documentView.areSearchDataComputed();
  }

  protected DocumentViewEntity() {
  }

  /**
   * @return the displayLicenseKey
   */
  protected String getDisplayLicenseKey() {
    return displayLicenseKey;
  }

  /**
   * @return the displayViewerPath
   */
  protected String getDisplayViewerPath() {
    return displayViewerPath;
  }

  /**
   * @return the urlBase
   */
  protected String getUrl() {
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

  /**
   * @return the language
   */
  protected String getLanguage() {
    return language;
  }

  /**
   * @return the nbPages
   */
  protected int getNbPages() {
    return nbPages;
  }

  /**
   * @return the documentSplit
   */
  protected boolean getDocumentSplit() {
    return documentSplit;
  }

  /**
   * @return the searchDataComputed
   */
  protected boolean getSearchDataComputed() {
    return searchDataComputed;
  }
}
