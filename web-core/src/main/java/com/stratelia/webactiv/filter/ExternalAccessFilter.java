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