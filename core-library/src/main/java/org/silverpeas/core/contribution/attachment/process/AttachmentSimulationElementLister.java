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
package org.silverpeas.core.contribution.attachment.process;

import org.silverpeas.kernel.exception.NotSupportedException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.process.annotation.SimulationElementLister;

/**
 * User: Yohann Chastagnier
 * Date: 24/10/13
 */
public class AttachmentSimulationElementLister extends SimulationElementLister {

  public AttachmentSimulationElementLister() {
    super();
  }

  public AttachmentSimulationElementLister(final SimulationElementLister parentElementLister) {
    super(parentElementLister);
  }

  @Override
  public void listElements(final ResourceReference sourcePK, final String language) {
    for (SimpleDocument document : AttachmentServiceProvider.getAttachmentService()
        .listAllDocumentsByForeignKey(sourcePK, language)) {
      addElement(new SimpleDocumentSimulationElement(document));
    }
  }

  @Override
  public void listElements(final Object source, final String language,
      final ResourceReference targetPK) {
    if (source instanceof SimpleDocument) {
      final SimpleDocument document = (SimpleDocument) source;
      if (getActionType().isMove() && document.getInstanceId().equals(targetPK.getInstanceId())) {
        return;
      }
      if (getActionType().isUpdate()) {
        SimpleDocument oldDocument = AttachmentServiceProvider.getAttachmentService()
            .searchDocumentById(document.getPk(), language);
        addElement(new SimpleDocumentSimulationElement(oldDocument).setOld());
      }
      addElement(new SimpleDocumentSimulationElement(document));
    } else {
      throw new NotSupportedException("This class expects a SimpleDocument as source");
    }
  }
}
