/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;

/**
 * Interface to be implemented by objects that can process requests
 */
public interface SILVERMAILRequestHandler {

  /**
   * Perform any processing requiring to support this request, and return the
   * URL of the JSP view that should display the results of the request.
   * 
   * @return the URL within the web application of the JSP view to which the
   *         controller should redirect the response
   */
  String handleRequest(ComponentSessionController componentSC,
      HttpServletRequest request) throws SILVERMAILException;

}
