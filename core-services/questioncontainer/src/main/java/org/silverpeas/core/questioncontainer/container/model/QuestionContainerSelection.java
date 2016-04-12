/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.questioncontainer.container.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.Date;

import org.silverpeas.core.index.indexing.model.IndexEntry;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SKDException;
import org.silverpeas.core.clipboard.SilverpeasKeyData;

import org.silverpeas.core.silvertrace.SilverTrace;

public class QuestionContainerSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = 1311812797166397833L;
  static public DataFlavor QuestionContainerDetailFlavor;

  static {
    try {
      QuestionContainerDetailFlavor = new DataFlavor(
          Class.forName("QuestionContainerDetail"),
          "QuestionContainer");
    } catch (ClassNotFoundException e) {
      SilverTrace.error("questionContainer", "QuestionContainerSelection",
          "error when initialize static QuestionContainerDetailFlavor", e);
      e.printStackTrace();
    }
  }

  private QuestionContainerDetail m_questionContainer;

  /**
   * @param questionContainer a question container detail
   */
  public QuestionContainerSelection(QuestionContainerDetail questionContainer) {
    super();
    m_questionContainer = questionContainer;
    super.addFlavor(QuestionContainerDetailFlavor);
  }

  @Override
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;
    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(QuestionContainerDetailFlavor)) {
        transferedData = m_questionContainer;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  @Override
  public IndexEntry getIndexEntry() {
    QuestionContainerPK questionContainerPK = m_questionContainer.getHeader().getPK();
    IndexEntry indexEntry =
        new IndexEntry(questionContainerPK.getComponentName(), "QuestionContainer",
            questionContainerPK.getId());
    indexEntry.setTitle(m_questionContainer.getHeader().getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(m_questionContainer.getHeader().getName());
    keyData.setAuthor(m_questionContainer.getHeader().getCreatorId());
    keyData.setCreationDate(new Date(m_questionContainer.getHeader().getCreationDate()));

    keyData.setDesc(m_questionContainer.getHeader().getDescription());
    try {
      keyData.setProperty("BEGINDATE", m_questionContainer.getHeader().getBeginDate());
      keyData.setProperty("ENDDATE", m_questionContainer.getHeader().getEndDate());
    } catch (SKDException e) {
      SilverTrace.error("questionContainer", "QuestionContainerSelection.getKeyData",
          "questionContainer.ERROR_KEY_DATA", e);
    }
    return keyData;
  }
}
