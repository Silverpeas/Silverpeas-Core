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
package org.silverpeas.core.contribution.attachment.process;

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.process.annotation.SimulationElementLister;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
  public void listElements(final WAPrimaryKey sourcePK, final String language) {
    for (SimpleDocument document : AttachmentServiceProvider.getAttachmentService()
        .listAllDocumentsByForeignKey(sourcePK, language)) {
      addElement(new SimpleDocumentSimulationElement(document));
    }
  }

  @Override
  public void listElements(final Object source, final String language, final WAPrimaryKey targetPK) {
    if (source instanceof SimpleDocument) {
      final SimpleDocument document = (SimpleDocument) source;
      if (getActionType().isMove()
          && document.getInstanceId().equals(targetPK.getInstanceId())) {
        return;
      }
      if (getActionType().isUpdate()) {
        SimpleDocument oldDocument = AttachmentServiceProvider.getAttachmentService()
            .searchDocumentById(document.getPk(), language);
        addElement(new SimpleDocumentSimulationElement(oldDocument).setOld());
      }
      addElement(new SimpleDocumentSimulationElement(document));
    } else {
      throw new NotImplementedException();
    }
  }
}
