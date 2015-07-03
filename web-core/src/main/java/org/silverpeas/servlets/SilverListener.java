/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package org.silverpeas.servlets;

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.cache.service.InMemoryCacheService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This class contains all Silverpeas's needs around WEB application lifecycles.
 */
public class SilverListener
    implements HttpSessionListener, ServletContextListener, ServletRequestListener {
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

  @Override
  public void requestDestroyed(final ServletRequestEvent sre) {
    // Clearing cache at this level avoids memory leaks
    clearRequestCache();
  }

  @Override
  public void requestInitialized(final ServletRequestEvent sre) {
    // Clearing cache at this level ensures that it is cleared before that all treatments behind the
    // request are performed.
    clearRequestCache();
    // Managing the session cache.
    ServletRequest request = sre.getServletRequest();
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      URLManager.setCurrentServerUrl(httpRequest);
      HttpSession httpSession = httpRequest.getSession(false);
      if (httpSession != null) {
        SessionInfo sessionInfo = SessionManagementFactory.getFactory().getSessionManagement()
            .getSessionInfo(httpSession.getId());
        if (sessionInfo != null) {
          CacheServiceFactory.getRequestCacheService()
              .put("@SessionCache@", sessionInfo.getCache());
        }
      }
    }
  }

  // Clear session informations
  private void remove(HttpSessionEvent event) {
    SessionManagementFactory factory = SessionManagementFactory.getFactory();
    SessionManagement sessionManagement = factory.getSessionManagement();
    sessionManagement.closeSession(event.getSession().getId());
    SilverTrace
        .info("peasCore", "SilverListener.sessionDestroyed", "peasCore.MSG_END_OF_HTTPSESSION",
            "ID=" + event.getSession().getId());
  }

  /**
   * Clears the cache associated to the request.
   */
  private void clearRequestCache() {
    CacheServiceFactory.getRequestCacheService().clear();
  }
}
