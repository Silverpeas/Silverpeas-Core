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

package com.stratelia.webactiv.servlets;

import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author sv To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SilverListener implements HttpSessionListener, ServletContextListener {
  // HttpSessionListener methods
  @Override
  public void sessionCreated(HttpSessionEvent event) {
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    remove(event);
  }

  // ServletContextListener methods
  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
  }

  // Clear session informations
  private void remove(HttpSessionEvent event) {
    SessionManagementFactory factory = SessionManagementFactory.getFactory();
    SessionManagement sessionManagement =  factory.getSessionManagement();
    sessionManagement.closeSession(event.getSession().getId());
    SilverTrace.info("peasCore", "SilverListener.sessionDestroyed",
        "peasCore.MSG_END_OF_HTTPSESSION", "ID=" + event.getSession().getId());
  }
}
