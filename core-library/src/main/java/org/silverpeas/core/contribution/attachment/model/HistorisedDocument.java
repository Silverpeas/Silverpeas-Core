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
package org.silverpeas.core.contribution.attachment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a document that is versioned. It exposes all data of a versioned document
 * from the master version to the first version. To obtain the master version, please use
 * {@link #getVersionMaster()} method (must return the same instance as the one of "this"). To check
 * if the current instance is indexed on master version, please use {@link #isVersionMaster()}
 * method.
 * @author ehugonnet
 */
public class HistorisedDocument extends SimpleDocument {
  private static final long serialVersionUID = -5850838926035340609L;

  private List<SimpleDocumentVersion> history;
  private List<SimpleDocumentVersion> functionalHistory;

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order,
      SimpleAttachment file) {
    super(pk, foreignId, order, true, file);
  }

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order, String owner,
      SimpleAttachment file) {
    super(pk, foreignId, order, true, owner, file);
  }

  public HistorisedDocument() {
    super(new SimpleDocumentPK(null), null, 0, true, SimpleAttachment.builder().build());
  }

  public HistorisedDocument(SimpleDocument doc) {
    super(doc);
    setVersionMaster(this);
  }

  @Override
  public HistorisedDocument getVersionMaster() {
    return (HistorisedDocument) super.getVersionMaster();
  }

  @Override
  public boolean isVersioned() {
    return true;
  }

  /**
   * Gets the technical history of the document versions (as they are stored in the JCR). In the
   * JCR, for a versioned item, each change drives to a new version, whatever the change is a
   * technical one (for example, disable the download of the document) or a functional one (the
   * content has been modified). This method returns the whole history of versions of the document.
   * @return the all the versions committed for this document.
   */
  public List<SimpleDocumentVersion> getHistory() {
    return history;
  }

  /**
   * Gets from the history the version identified by the given identifier.
   * @param id the identifier of the searched version.
   * @return the version of the simple document which the identifier is the one specified, null
   * otherwise.
   */
  public SimpleDocumentVersion getVersionIdentifiedBy(String id) {
    for (SimpleDocumentVersion version : history) {
      if (version.getId().equals(id)) {
        return version;
      }
    }
    return null;
  }

  /**
   * Gets the functional history based on the versions and the indexes of this document. The
   * returned versions refer each of them the result of modification of its content.
   * @return the different functional versions of this document.
   */
  public List<SimpleDocumentVersion> getFunctionalHistory() {
    if (functionalHistory == null && history != null) {
      functionalHistory = new ArrayList<>(history.size());
      String lastVersion = getVersion();
      for (SimpleDocumentVersion currentDocumentVersion : history) {
        String currentVersion = currentDocumentVersion.getVersion();
        if (!currentVersion.equals(lastVersion) &&
            currentDocumentVersion.getVersionIndex() < getVersionIndex()) {
          functionalHistory.add(currentDocumentVersion);
        }
        lastVersion = currentVersion;
      }
    }
    return functionalHistory;
  }

  public void setHistory(List<SimpleDocumentVersion> history) {
    this.history = history;
  }

  public List<SimpleDocument> getPublicVersions() {
    List<SimpleDocument> publicVersions =
        new ArrayList<>(getFunctionalHistory().size());
    for (SimpleDocument document : getFunctionalHistory()) {
      if (document.isPublic()) {
        publicVersions.add(document);
      }
    }
    return publicVersions;
  }

  /**
   * Returns the more recent public version of this document - null if none exists.
   * @return the more recent public version of this document - null if none exists.
   */
  @Override
  public SimpleDocument getLastPublicVersion() {
    if (this.isPublic()) {
      return this;
    }
    for (SimpleDocument document : getFunctionalHistory()) {
      if (document.isPublic()) {
        return document;
      }
    }
    return null;
  }

  public SimpleDocumentVersion getPreviousVersion() {
    if (!getHistory().isEmpty()) {
      return getHistory().get(0);
    }
    return null;
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
