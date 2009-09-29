/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail.requesthandlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILException;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILRequestHandler;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class Compose implements SILVERMAILRequestHandler {

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
    return "/SILVERMAIL/jsp/main.jsp";
  }

}
