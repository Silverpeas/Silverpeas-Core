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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.filter;

import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.authentication.security.SecurityData;
import com.stratelia.silverpeas.authentication.security.SecurityHolder;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ExternalAccessFilter implements Filter {

  private static ResourceLocator generalSettings = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    if (generalSettings == null) {
      generalSettings = new ResourceLocator("com.stratelia.webactiv.general", "");
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String encoding = generalSettings.getString("charset", "UTF-8");
    req.setCharacterEncoding(encoding);
    String securityId = req.getParameter("securityId");
    if (securityId != null) {
      HttpSession session = req.getSession(true);
      MainSessionController controller = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

      SecurityData securityData = SecurityHolder.getData(securityId);
      if (securityData != null) {
        if ((controller == null)
            || (!controller.getCurrentUserDetail().getLogin().equals(
            securityData.getUserId()))) {
          LoginPasswordAuthentication authentication = new LoginPasswordAuthentication();
          String key = authentication.authenticate(securityData.getUserId(),
              securityData.getDomainId(), req);

          try {
            controller = new MainSessionController(key, session.getId());
          } catch (Exception e) {
            SilverTrace.error("util",
                "ExternalAccessFilter.doFilter()", "root.MSG_GEN_EXIT_METHOD", e);
          }
          // Init session management and session object.
          SessionManager.getInstance().addSession(session, req, controller);
          // Put the main session controller in the session
          session.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, controller);
          GraphicElementFactory gef = new GraphicElementFactory(controller.getFavoriteLook());
          String stylesheet = req.getParameter("stylesheet");
          if (stylesheet != null) {
            // To use a specific stylesheet.
            gef.setExternalStylesheet(stylesheet);
          }
          session.setAttribute("SessionGraphicElementFactory", gef);
        }
      }
    }
    chain.doFilter(request, response);
  }
}
