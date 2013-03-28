/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package org.silverpeas.wysiwyg;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import org.silverpeas.attachment.SimpleDocumentInstanciator;

import java.sql.Connection;

public class WysiwygInstanciator extends SQLRequest {

  /**
   * Creates new WysiwygInstanciator
   */
  public WysiwygInstanciator() {
  }

  public WysiwygInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.wysiwyg");
  }


  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.create", "Finished");
  }

  /**
   * Method declaration call the method delete SimpleDocumentInstanciator class.
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * @throws com.silverpeas.admin.components.InstanciationException
   * @see
   */
  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    new SimpleDocumentInstanciator().delete(componentId);
    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.delete", "finished");

  }

}
