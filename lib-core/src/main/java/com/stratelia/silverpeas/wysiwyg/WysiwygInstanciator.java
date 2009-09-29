/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.silverpeas.wysiwyg;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;

/**
 * 
 * @author pchaille
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
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
   * 
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * 
   * @throws InstanciationException
   * 
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
   * 
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * 
   * @throws InstanciationException
   * 
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
