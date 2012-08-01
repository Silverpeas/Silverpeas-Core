/**
 * >>>>>>> master:lib-core/src/main/java/com/silverpeas/jcrutil/model/AbstractJcrDao.java Copyright
 * (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection withWriter Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.model;

import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class HistorisedDocument extends SimpleDocument {

  private List<SimpleDocument> history;

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order, SimpleAttachment file) {
    super(pk, foreignId, order, true, file);
  }

  public HistorisedDocument(SimpleDocumentPK pk, String foreignId, int order, String owner,
      SimpleAttachment file) {
    super(pk, foreignId, order, true, owner, file);
  }

  public HistorisedDocument() {
  }

  public HistorisedDocument(SimpleDocument doc) {
    super(doc.getPk(), doc.getForeignId(), doc.getOrder(), true, doc.getEditedBy(), doc.
        getReservation(), doc.getAlert(), doc.getExpiry(), doc.getStatus(), doc.getFile());
    setMajorVersion(doc.getMajorVersion());
    setMinorVersion(doc.getMinorVersion());
  }

  public List<SimpleDocument> getHistory() {
    return history;
  }

  public void setHistory(List<SimpleDocument> history) {
    this.history = history;
  }
}
