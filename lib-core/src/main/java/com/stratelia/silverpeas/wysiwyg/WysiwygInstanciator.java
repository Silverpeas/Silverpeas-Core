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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.wysiwyg;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;

public class WysiwygInstanciator extends SQLRequest {

  /**
   * Creates new WysiwygInstanciator
   */
  public WysiwygInstanciator() {
  }

  public WysiwygInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.wysiwyg");
  }

  /**
   * Method declaration call the method create AttachmentInstanciator class.
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * @throws InstanciationException
   * @see
   */
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.create", "Begin");

    AttachmentInstanciator ai = new AttachmentInstanciator(
        "com.stratelia.webactiv.wysiwyg");
    ai.create(con, spaceId, componentId, userId);

    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.create", "Finished");

  }

  /**
   * Method declaration call the method delete AttachmentInstanciator class.
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * @throws InstanciationException
   * @see
   */
  public void delete(Connection con, String spaceId, String componentId,
      String userId) {
    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.delete",
        "delete called with: space=" + spaceId);

    AttachmentInstanciator ai = new AttachmentInstanciator(
        "com.stratelia.webactiv.wysiwyg");
    try {
      ai.delete(con, spaceId, componentId, userId);
    } catch (InstanciationException ie) {
      SilverTrace.error("wysiwyg", "wysiwygInstanciator.delete",
          "wysiwyg.DELETING_FAILED", ie);
    }

    SilverTrace.debug("wysiwyg", "WysiwygInstanciator.delete", "finished");

  }

}
