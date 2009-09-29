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
