/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.util.URLUtil;

/**
 * This class represents one version in the history of a versioned simple document.
 * <p/>
 * The identifier of the component instance provided by this class is the one of the head version.
 * It is the same thing for the provided foreign identifier information.
 * To retrieve the real historised values of the version, please use methods for which the name
 * prefix is <b>getRealVersion</b>...
 * @author Yohann Chastagnier
 */
public class SimpleDocumentVersion extends SimpleDocument {
  private static final long serialVersionUID = 6383649345169447613L;

  private SimpleDocumentVersion previousVersion;
  private SimpleDocumentPK realVersionPk;
  private String realVersionForeignId;

  /**
   * The default constructor of a simple document version.
   * @param documentVersion the original version.
   * @param masterVersion
   */
  public SimpleDocumentVersion(final SimpleDocument documentVersion,
      final HistorisedDocument masterVersion) {
    super(documentVersion);
    setVersionMaster(masterVersion);
  }

  @Override
  public HistorisedDocument getVersionMaster() {
    return (HistorisedDocument) super.getVersionMaster();
  }

  @Override
  public void setVersionMaster(final SimpleDocument versionMaster) {
    if (!(versionMaster instanceof HistorisedDocument)) {
      throw new IllegalArgumentException("The master version must be an historised one ...");
    }
    super.setVersionMaster(versionMaster);
    if (realVersionPk == null) {
      realVersionPk = getPk().clone();
      realVersionForeignId = getForeignId();
    }
    getPk().setComponentName(getVersionMaster().getInstanceId());
    getPk().setOldSilverpeasId(getVersionMaster().getOldSilverpeasId());
    setForeignId(getVersionMaster().getForeignId());
    setNodeName(getVersionMaster().getNodeName());
  }

  public SimpleDocumentVersion getPreviousVersion() {
    return previousVersion;
  }

  public void setPreviousVersion(final SimpleDocumentVersion previousVersion) {
    this.previousVersion = previousVersion;
  }

  /**
   * Gets the real value of the PK of the version and not the one of the head version.
   * @return the historised PK value of the historised version.
   */
  public SimpleDocumentPK getRealVersionPk() {
    if (realVersionPk == null) {
      return getPk();
    }
    return realVersionPk;
  }

  /**
   * Gets the real value of the foreign identifier of the version and not the one of the head
   * version.
   * @return the historised foreign identifier value of the historised version.
   */
  public String getRealVersionForeignId() {
    if (realVersionForeignId == null) {
      return getForeignId();
    }
    return realVersionForeignId;
  }

  @Override
  public boolean isVersioned() {
    return true;
  }

  @Override
  public SimpleDocument getLastPublicVersion() {
    SimpleDocumentVersion current = this;
    while (current != null) {
      if (current.isPublic()) {
        return current;
      }
      current = current.getPreviousVersion();
    }
    return null;
  }

  @Override
  public String getWebdavUrl() {
    return null;
  }

  @Override
  public String getOnlineURL() {
    return null;
  }

  @Override
  public String getAttachmentURL() {
    return super.getAttachmentURL();
  }

  @Override
  public String getUniversalURL() {
    return URLUtil.getSimpleURL(URLUtil.URL_VERSION, getId()) + "?ContentLanguage=" +
        getLanguage();
  }
}
