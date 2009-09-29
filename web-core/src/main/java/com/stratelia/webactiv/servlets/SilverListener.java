/*
 * Created on 3 déc. 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.stratelia.webactiv.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.*;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author sv
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SilverListener implements HttpSessionListener,
    ServletContextListener {
  // HttpSessionListener methods
  public void sessionCreated(HttpSessionEvent event) {
  }

  public void sessionDestroyed(HttpSessionEvent event) {
    remove(event);
  }

  // ServletContextListener methods
  public void contextDestroyed(ServletContextEvent event) {
    SilverTrace.info("peasCore", "SilverListener.contextDestroyed",
        "peasCore.MSG_END_OF_HTTPSESSION");
    SessionManager.getInstance().shutdown();
  }

  public void contextInitialized(ServletContextEvent event) {
  }

  // Clear session informations
  private void remove(HttpSessionEvent event) {
    SessionManager mgr = SessionManager.getInstance();
    mgr.removeSession(event.getSession());
    SilverTrace.info("peasCore", "SilverListener.sessionDestroyed",
        "peasCore.MSG_END_OF_HTTPSESSION", "ID=" + event.getSession().getId());
  }
}
