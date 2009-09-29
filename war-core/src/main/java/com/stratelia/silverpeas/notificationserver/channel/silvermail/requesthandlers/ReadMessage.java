/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail.requesthandlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILException;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILSessionController;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class ReadMessage implements SILVERMAILRequestHandler {

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
      String sId = (String) request.getParameter("ID");
      long ID = Long.parseLong(sId);

      ((SILVERMAILSessionController) componentSC).setCurrentMessageId(ID);
    } catch (NumberFormatException e) {
      SilverTrace.error("silvermail", "ReadMessage.handleRequest()",
          "root.EX_IGNORED", "", e);
    }
    return "/SILVERMAIL/jsp/readMessage.jsp";
  }

}
