/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail.requesthandlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILException;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILPersistence;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class DeleteAllMessages implements SILVERMAILRequestHandler {

  /**
   * Method declaration
   * 
   * 
   * @param componentSC
   * @param request
   * 
   * @return
   * 
   * @throws SILVERMAILException
   * 
   * @see
   */
  public String handleRequest(ComponentSessionController componentSC,
      HttpServletRequest request) throws SILVERMAILException {
    try {
      SILVERMAILPersistence.deleteAllMessages(Integer.parseInt(componentSC
          .getUserId()), request.getParameter("folder"));
    } catch (NumberFormatException e) {
      SilverTrace.error("silvermail", "DeleteAllMessages.handleRequest()",
          "root.EX_IGNORED", "", e);
    }

    if (request.getParameter("from") == null) {
      return "/SILVERMAIL/jsp/main.jsp";
    } else if (request.getParameter("from").equals("homePage")) {
      return "/SILVERMAIL/jsp/redirect.jsp?SpaceId="
          + request.getParameter("SpaceId");
    } else
      return "/SILVERMAIL/jsp/main.jsp";
  }

}
