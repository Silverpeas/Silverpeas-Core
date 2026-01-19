/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.driver.admin;

import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployerException;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdaterUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.web.portlets.portal.DesktopMessages;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UploadServlet is responsible for uploading the portlet war file
 */
public class UploadServlet extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 6041525805480787611L;

  private ServletContext context;

  private static final Logger logger = Logger.getLogger(UploadServlet.class
      .getPackage().getName(), "org.silverpeas.portlets.PCDLogMessages");

  @Override
  public void init(ServletConfig config) throws ServletException {
    // TODO Auto-generated method stub
    super.init(config);
    context = config.getServletContext();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (!User.getCurrentRequester().isAccessAdmin()) {
      throwHttpForbiddenError();
    }

    // Initialize DesktopMessages' Resource Bundle
    DesktopMessages.init(getLanguage());
    try {
      uploadFile(request);
    } catch (Exception pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0029", pre);
    } finally {
      RequestDispatcher reqd = context
          .getRequestDispatcher("/portlet/jsp/jsr/deployer.jsp");
      reqd.forward(request, response);
    }
  }

  /*
   * This method below is for use with commons-fileupload version 1.1
   */
  private void uploadFile(HttpServletRequest request) throws PortletRegistryException {

    HttpSession session = AdminUtils.getClearedSession(request);
    List<FileItem> fileItems = FileUploadUtil.parseRequest(request);
    String[] fileNames = new String[2];
    Iterator<FileItem> itr = fileItems.iterator();
    int i = 0;
    while (itr.hasNext()) {
      FileItem fi = itr.next();
      if (!fi.isFormField()) {
        fileNames[i] = processFileItem(fi);
        i++;
      }
    }
    deployPortlet(fileNames, session);
    // refresh portlet list
    AdminUtils.refreshList(request, getLanguage());
  }

  // First item is portlet war, second item is roles file
  private void deployPortlet(String[] fileNames, HttpSession session)
      throws PortletRegistryException {
    String warFileName = fileNames[0];
    if (warFileName == null || !warFileName.endsWith(".war")) {
      session.setAttribute(AdminConstants.DEPLOYMENT_FAILED_ATTRIBUTE,
          DesktopMessages.getLocalizedString(AdminConstants.INVALID_PORTLET_APP));
    } else {
      PortletAdminData portletAdminData = PortletAdminDataFactory.getPortletAdminData(null);
      boolean success;
      StringBuilder messageBuffer = new StringBuilder();
      try {
        // If already deployed. Unregister it before deploying
        if (isPortletDeployed(warFileName)) {
          try {
            portletAdminData.undeploy(getWarName(warFileName), false);
          } catch (Exception ex) {
            // ignored
          }
        }
        success = portletAdminData.deploy(warFileName, fileNames[1], null, true);
        messageBuffer.append(DesktopMessages
            .getLocalizedString(AdminConstants.DEPLOYMENT_SUCCEEDED));
      } catch (Exception ex) {
        success = false;
        if (ex instanceof WebAppDeployerException) {
          Object[] tokens = { PortletRegistryHelper
              .getUpdatedAbsoluteWarFileName(warFileName) };
          messageBuffer.append(DesktopMessages.getLocalizedString(
              AdminConstants.WAR_NOT_DEPLOYED, tokens));
        } else {
          messageBuffer.append(DesktopMessages
              .getLocalizedString(AdminConstants.DEPLOYMENT_FAILED));
          messageBuffer.append(".");
          messageBuffer.append(ex.getMessage());
          // Undeploy only when deploy fails for reasons other than war
          // deployment
          try {
            portletAdminData.undeploy(getWarName(warFileName), true);
          } catch (Exception ex1) {
            // ignored
          }
        }
      }
      if (success) {
        session.setAttribute(AdminConstants.DEPLOYMENT_SUCCEEDED_ATTRIBUTE,
            messageBuffer.toString());
      } else {
        session.setAttribute(AdminConstants.DEPLOYMENT_FAILED_ATTRIBUTE,
            messageBuffer.toString());
      }
      new File(warFileName).delete();
    }
  }

  private String processFileItem(FileItem fi) {

    // On some browsers fi.getName() will return the full path to the file
    // the client select this can cause problems
    // so the following is a workaround.
    try {
      String fileName = fi.getFileName();
      if (fileName == null || fileName.trim().isEmpty()) {
        return null;
      }
      fileName = FilenameUtils.getName(fileName);

      File fNew = File.createTempFile("opc", ".tmp");
      fNew.deleteOnExit();
      fi.saveTo(fNew);

      File finalFileName = new File(fNew.getParent() + File.separator
          + fileName);
      if (!fNew.renameTo(finalFileName)) {
        // unable to rename, copy the contents of the file instead
        PortletWarUpdaterUtil.copyFile(fNew, finalFileName, true, false);
      }
      return finalFileName.getAbsolutePath();

    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage());
    }
  }

  private String getWarName(String warFileName) {
    String warName = PortletWarUpdaterUtil.getWarName(warFileName);
    String regexp = WarFileFilter.WAR_EXTENSION + "$";
    return warName.replaceFirst(regexp, "");
  }

  private boolean isPortletDeployed(String warFileName)
      throws PortletRegistryException {

    String filename = PortletRegistryHelper.getWarFileLocation()
        + File.separator + PortletWarUpdaterUtil.getWarName(warFileName);
    return (new File(filename)).exists();
  }

  private String getLanguage() {
    return User.getCurrentRequester().getUserPreferences().getLanguage();
  }
}
