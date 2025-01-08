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
package org.silverpeas.core.questioncontainer.container.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SKDException;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

public class QuestionContainerSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = 1311812797166397833L;
  private static final String TYPE = "QuestionContainer";
  public static final DataFlavor QuestionContainerDetailFlavor =
      new DataFlavor(QuestionContainerDetail.class, TYPE);

  private final QuestionContainerDetail questionContainer;

  /**
   * @param questionContainer a question container detail
   */
  public QuestionContainerSelection(QuestionContainerDetail questionContainer) {
    super();
    this.questionContainer = questionContainer;
    super.addFlavor(QuestionContainerDetailFlavor);
  }

  @Override
  @Nonnull
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;
    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(QuestionContainerDetailFlavor)) {
        transferedData = questionContainer;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  @Override
  public IndexEntry getIndexEntry() {
    QuestionContainerPK questionContainerPK = questionContainer.getHeader().getPK();
    IndexEntry indexEntry =
        new IndexEntry(new IndexEntryKey(questionContainerPK.getComponentName(),
            TYPE, questionContainerPK.getId()));
    indexEntry.setTitle(questionContainer.getHeader().getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData(questionContainer.getId(),
        questionContainer.getComponentInstanceId());

    keyData.setTitle(questionContainer.getHeader().getName());
    keyData.setAuthor(questionContainer.getHeader().getCreatorId());
    keyData.setCreationDate(questionContainer.getHeader().getCreationDate());
    keyData.setDesc(questionContainer.getHeader().getDescription());
    keyData.setType(TYPE);
    if (questionContainer.getComponentInstanceId().startsWith("survey")) {
      keyData.setLink(URLUtil.getSimpleURL(URLUtil.URL_SURVEY, questionContainer.getId(),
          questionContainer.getComponentInstanceId()));
    } else {
      keyData.setLink(URLUtil.getSimpleURL(URLUtil.URL_COMPONENT,
          questionContainer.getComponentInstanceId()));
    }
    try {
      if (questionContainer.getHeader().getBeginDate() != null) {
        keyData.setProperty("BEGINDATE", questionContainer.getHeader().getBeginDate());
      }
      if (questionContainer.getHeader().getEndDate() != null) {
        keyData.setProperty("ENDDATE", questionContainer.getHeader().getEndDate());
      }
    } catch (SKDException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return keyData;
  }
}
