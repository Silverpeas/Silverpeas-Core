/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection withWriter Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class HistorisedDocument extends SimpleDocument {
  private static final long serialVersionUID = -5850838926035340609L;

  private List<SimpleDocument> history;
  private List<SimpleDocument> functionalHistory;

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order, SimpleAttachment file) {
    super(pk, foreignId, order, true, file);
  }

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order, String owner,
      SimpleAttachment file) {
    super(pk, foreignId, order, true, owner, file);
  }

  public HistorisedDocument() {
    super(new SimpleDocumentPK(null), null, 0, true, new SimpleAttachment());
  }

  public HistorisedDocument(SimpleDocument doc) {
    super(doc.getPk(), doc.getForeignId(), doc.getOrder(), true, doc.getEditedBy(), doc.
        getReservation(), doc.getAlert(), doc.getExpiry(), doc.getComment(), doc.getFile());
    setRepositoryPath(doc.getRepositoryPath());
    setVersionIndex(doc.getVersionIndex());
    setVersionMaster(doc.getVersionMaster());
    setMajorVersion(doc.getMajorVersion());
    setMinorVersion(doc.getMinorVersion());
    setStatus(doc.getStatus());
    setPublicDocument(doc.isPublic());
    setNodeName(doc.getNodeName());
  }

  @Override
  public boolean isVersioned() {
    return true;
  }

  /**
   * Returns technical history (as the JCR)
   * @return
   */
  public List<SimpleDocument> getHistory() {
    return history;
  }

  /**
   * Returns functional history based on versions and indexes.
   * @return
   */
  public List<SimpleDocument> getFunctionalHistory() {
    if (functionalHistory == null && history != null) {
      functionalHistory = new ArrayList<SimpleDocument>(history.size());
      String lastVersion = getVersionMaster().getVersion();
      for (SimpleDocument currentDocumentVersion : history) {
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

  public void setHistory(List<SimpleDocument> history) {
    this.history = history;
  }

  public List<SimpleDocument> getPublicVersions() {
    List<SimpleDocument> publicVersions =
        new ArrayList<SimpleDocument>(getFunctionalHistory().size());
    for (SimpleDocument document : getFunctionalHistory()) {
      if (document.isPublic()) {
        publicVersions.add(document);
      }
    }
    return publicVersions;
  }

  /**
   * Returns the more recent public version of this document - null if none exists.
   *
   * @return the more recent public version of this document - null if none exists.
   */
  @Override
  public SimpleDocument getLastPublicVersion() {
    if (this.isPublic()) {
      return this;
    }
    for (SimpleDocument document : getFunctionalHistory()) {
      if (document.isPublic()) {
        HistorisedDocument historisedDocument = new HistorisedDocument(document);
        historisedDocument.setHistory(history);
        return historisedDocument;
      }
    }
    return null;
  }
}
