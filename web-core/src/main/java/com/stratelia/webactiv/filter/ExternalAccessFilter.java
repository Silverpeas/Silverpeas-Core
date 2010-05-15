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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.authentication.security.SecurityData;
import com.stratelia.silverpeas.authentication.security.SecurityHolder;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class ExternalAccessFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;

    String securityId = req.getParameter("securityId");
    if (securityId != null) {
      HttpSession session = req.getSession(true);
      MainSessionController controller = (MainSessionController) session
          .getAttribute("SilverSessionController");

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
            // TODO Auto-generated catch block
            e.printStackTrace();
            // Affichage page erreur
          }

          // Init session management and session object.
          SessionManager.getInstance().addSession(session, req, controller);

          // Put the main session controller in the session
          session.setAttribute("SilverSessionController", controller);

          GraphicElementFactory gef = new GraphicElementFactory(controller
              .getFavoriteLook());
          String stylesheet = req.getParameter("stylesheet");
          if (stylesheet != null) {
            // To use a specific stylesheet.
            gef.setExternalStylesheet(stylesheet);
          }
          session.setAttribute("SessionGraphicElementFactory", gef);
        }
      } else {
        // Affichage page erreur
      }
    }

    chain.doFilter(request, response);
  }

  public FilterConfig getFilterConfig() {
    return null;
  }

  public void setFilterConfig(FilterConfig arg0) {
  }

}