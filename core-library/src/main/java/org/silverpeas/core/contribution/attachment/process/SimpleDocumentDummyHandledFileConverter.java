/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.contribution.attachment.process;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.core.process.io.file.DummyHandledFile;

import java.util.LinkedList;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
@Service
public class SimpleDocumentDummyHandledFileConverter
    extends AbstractDummyHandledFileConverter<SimpleDocumentSimulationElement> {

  @Override
  public List<DummyHandledFile> convert(final List<SimpleDocumentSimulationElement> elements,
      final WAPrimaryKey targetPK, final ActionType actionType) {

    // Initializing the result
    List<DummyHandledFile> dummyHandledFiles = new LinkedList<>();

    // For now, only move and copy actions are handled
    if (actionType.isCreate() || actionType.isUpdate() || actionType.isCopy() ||
        actionType.isMove()) {

      for (SimpleDocumentSimulationElement document : elements) {

        // Adding the dummy representation of the source document in case of update and if the
        // current element is an old one (deletion)
        // Adding the dummy representation of the source document in case of move and if the
        // current element is not an old one(deletion)
        if ((actionType.isUpdate() && document.isOld()) ||
            (actionType.isMove() && !document.isOld())) {
          dummyHandledFiles.add(new SimpleDocumentDummyHandledFile(document.getElement(), true));
        }

        // Adding the dummy representation of the target file, if the current element is not an
        // old one
        if (!document.isOld()) {
          dummyHandledFiles
              .add(new SimpleDocumentDummyHandledFile(document.getElement(), targetPK));
        }
      }
    } else {
      throw new NotSupportedException("The action type isn't supported by this class");
    }

    // Result
    return dummyHandledFiles;
  }
}
