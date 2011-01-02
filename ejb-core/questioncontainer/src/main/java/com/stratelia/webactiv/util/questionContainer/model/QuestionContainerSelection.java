/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.questionContainer.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SKDException;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

public class QuestionContainerSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = 1311812797166397833L;
  static public DataFlavor QuestionContainerDetailFlavor;

  static {
    try {
      QuestionContainerDetailFlavor =
          new DataFlavor(
              Class
                  .forName("com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail"),
              "QuestionContainer");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private QuestionContainerDetail m_questionContainer;

  /**
   * ----------------------------------------------------------------------------------------------
   * ---------- Constructor
   */
  public QuestionContainerSelection(QuestionContainerDetail questionContainer) {
    super();
    m_questionContainer = questionContainer;
    super.addFlavor(QuestionContainerDetailFlavor);
  }

  /**
   * ----------------------------------------------------------------------------------------------
   * ----------
   */
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

  /**
   * ----------------------------------------------------------------------------------------------
   * ----------
   */
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry;
    QuestionContainerPK questionContainerPK = m_questionContainer.getHeader().getPK();
    indexEntry =
        new IndexEntry(questionContainerPK.getComponentName(), "QuestionContainer",
            questionContainerPK.getId());
    indexEntry.setTitle(m_questionContainer.getHeader().getName());
    return indexEntry;
  }

  /**
   * ----------------------------------------------------------------------------------------------
   * ---------- Tranformation obligatoire en SilverpeasKeyData
   */
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
