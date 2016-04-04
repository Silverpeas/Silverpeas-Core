/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package com.sun.portal.portletcontainer.driver.admin;

import org.silverpeas.web.portlets.portal.DesktopMessages;
import org.silverpeas.web.portlets.portal.PropertiesContext;
import org.silverpeas.core.admin.user.model.UserDetail;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployerException;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminServlet is a router for admin related requests like deploying/undeploying of portlets and
 * creating of portlet windows.
 */
public class PortletDeployerServlet extends HttpServlet {
  private static final long serialVersionUID = 7041695476364573175L;

  private static final Logger logger = Logger.getLogger(PortletDeployerServlet.class
      .getPackage().getName(), "org.silverpeas.portlets.PCDLogMessages");
  private static final String PORTLET_DRIVER_AUTODEPLOY_DIR = PortletRegistryHelper
      .getAutoDeployLocation();

  private ServletContext context;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    context = config.getServletContext();
    PropertiesContext propertiesContext = PropertiesContext.get();
    // Do not invoke autodeploy is not enabled
    if (propertiesContext.enableAutodeploy()) {
      DirectoryWatcherTask watcher = new DirectoryWatcherTask(
          PORTLET_DRIVER_AUTODEPLOY_DIR, new WarFileFilter(),
          new DirectoryChangedListener() {

        @Override
        public void fileAdded(File file) {
          if (file.getName().endsWith(WarFileFilter.WAR_EXTENSION)) {
            PortletWar portlet = new PortletWar(file);
            checkAndDeploy(portlet);
          } else if (file.getName().endsWith(
              WarFileFilter.WAR_DEPLOYED_EXTENSION)) {
            String markerFileName = file.getAbsolutePath();
            String portletWarFileName = markerFileName.replaceFirst(
                WarFileFilter.WAR_DEPLOYED_EXTENSION + "$", "");
            PortletWar portlet = new PortletWar(portletWarFileName);

            if (!portlet.warFileExists()) {
              try {
                portlet.undeploy();
              } catch (Exception e) {
                if (logger.isLoggable(Level.INFO)) {
                  logger.log(Level.INFO, "PSPCD_CSPPD0031", portlet
                      .getWarName());
                }
              }
            }
          }
          }

        private void checkAndDeploy(PortletWar portlet) {
          if (!portlet.isDeployed())
            portlet.deploy();
          else if (portlet.needsRedeploy()) {
            try {
              portlet.redeploy();
            } catch (Exception e) {
              if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "PSPCD_CSPPD0031", portlet
                    .getWarName());
              }
            }
          }
          }

      });

      Timer timer = new Timer();
      long watchInterval = propertiesContext.getAutodeployDirWatchInterval();
      timer.schedule(watcher, watchInterval, watchInterval);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGetPost(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGetPost(request, response);
  }

  public void doGetPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String language = getLanguage(request);

    DesktopMessages.init(language);
    response.setContentType("text/html;charset=UTF-8");
    HttpSession session = AdminUtils.getClearedSession(request);
    PortletAdminData portletAdminData;
    try {
      portletAdminData = PortletAdminDataFactory.getPortletAdminData(null);
    } catch (PortletRegistryException pre) {
      throw new IOException(pre.getMessage());
    }
    AdminUtils.setAttributes(session, portletAdminData, "useless", "useless",
        "useless", language);

    if (isParameterPresent(request, AdminConstants.UNDEPLOY_PORTLET_SUBMIT)) {
      String[] portletsToUndeploy = request
          .getParameterValues(AdminConstants.PORTLETS_TO_UNDEPLOY);
      if (portletsToUndeploy == null) {
        String message = DesktopMessages
            .getLocalizedString(AdminConstants.NO_PORTLET_APP);
        session.setAttribute(AdminConstants.UNDEPLOYMENT_FAILED_ATTRIBUTE,
            message);
      } else {
        StringBuilder messageBuffer = new StringBuilder();
        boolean success = false;
        for (String warName : portletsToUndeploy) {
          try {
            success = portletAdminData.undeploy(warName, true);
          } catch (Exception ex) {
            success = false;
            if (ex instanceof WebAppDeployerException) {
              Object[] tokens = {warName + ".war"};
              messageBuffer.append(
                  DesktopMessages.getLocalizedString(AdminConstants.WAR_NOT_UNDEPLOYED, tokens));
            } else {
              messageBuffer
                  .append(DesktopMessages.getLocalizedString(AdminConstants.UNDEPLOYMENT_FAILED));
              messageBuffer.append(".");
              messageBuffer.append(ex.getMessage());
            }
            // If undeploy throws exception, stop undeploying remaining portlets
            break;
          }
        }

        if (success) {
          messageBuffer.append(DesktopMessages
              .getLocalizedString(AdminConstants.UNDEPLOYMENT_SUCCEEDED));
          session.setAttribute(AdminConstants.UNDEPLOYMENT_SUCCEEDED_ATTRIBUTE,
              messageBuffer.toString());
          // refresh portlet list
          AdminUtils.refreshList(request, language);
        } else {
          session.setAttribute(AdminConstants.UNDEPLOYMENT_FAILED_ATTRIBUTE,
              messageBuffer.toString());
        }
      }
    } else {
      try {
        AdminUtils.setPortletWindowAttributes(session, portletAdminData, null);
      } catch (Exception ex) {
        StringBuilder messageBuffer = new StringBuilder(DesktopMessages
            .getLocalizedString(AdminConstants.NO_WINDOW_DATA));
        messageBuffer.append(".");
        messageBuffer.append(ex.getMessage());
        session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE,
            messageBuffer.toString());
      }
    }

    RequestDispatcher reqd = context
        .getRequestDispatcher("/portlet/jsp/jsr/deployer.jsp");
    reqd.forward(request, response);
  }

  private boolean isParameterPresent(HttpServletRequest request,
      String parameter) {
    String name = request.getParameter(parameter);
    return (name != null);
  }

  private String getLanguage(HttpServletRequest request) {
    return UserDetail.getCurrentRequester().getUserPreferences().getLanguage();
  }
}
