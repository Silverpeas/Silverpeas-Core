/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.jcr.JcrRepositoryConnector;
import org.silverpeas.jcr.JcrSession;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Clean the JCR repository for all documents concerning the specified component instance
 * identifier.
 *
 * @author ehugonnet
 */
public class SimpleDocumentInstanciator {

  /**
   * Clean the JCR repository for all documents concerning the specified compoentId.
   *
   * @param componentId
   * @throws InstanciationException
   */
  public void delete(String componentId) throws InstanciationException {
    try (JcrSession session = JcrRepositoryConnector.openSystemSession()) {
      session.getNode('/' + componentId).remove();
      session.save();
    } catch(PathNotFoundException pnfe) {
      SilverTrace.warn("attachment", "SimpleDocumentInstanciator.delete",
          "root.DELETING_DATA_DIRECTORY_FAILED", "JCR Path '/" + componentId + "' does not exist");
    } catch (RepositoryException ex) {
      throw new InstanciationException(ex);
    }
  }
}
