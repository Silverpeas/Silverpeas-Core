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
import org.silverpeas.core.admin.user.model.UserDetail;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployerException;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdaterUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.SilverpeasDiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UploadServlet is responsible for uploading the portlet war file
 */
public class UploadServlet extends HttpServlet {

  private static final long serialVersionUID = 6041525805480787611L;

  private ServletContext context;

  private long maxUploadSize;

  private static final Logger logger = Logger.getLogger(UploadServlet.class
      .getPackage().getName(), "org.silverpeas.portlets.PCDLogMessages");

  @Override
  public void init(ServletConfig config) throws ServletException {
    // TODO Auto-generated method stub
    super.init(config);
    maxUploadSize = config.getInitParameter("MAX_UPLOAD_SIZE") == null ? 10000000
        : Integer.parseInt(config.getInitParameter("MAX_UPLOAD_SIZE"));
    context = config.getServletContext();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Initialize DesktopMessages' Resource Bundle
    DesktopMessages.init(getLanguage(request));
    try {
      uploadFile(request, response);
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0029", pre);
    } catch (FileUploadException e) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0029", e);
    } finally {
      RequestDispatcher reqd = context
          .getRequestDispatcher("/portlet/jsp/jsr/deployer.jsp");
      reqd.forward(request, response);
    }
  }

  /*
   * This method below is for use with commons-fileupload version 1.1
   */
  private void uploadFile(HttpServletRequest request,
      HttpServletResponse response) throws FileUploadException, PortletRegistryException {

    HttpSession session = AdminUtils.getClearedSession(request);

    SilverpeasDiskFileItemFactory factory = new SilverpeasDiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(maxUploadSize);

    // Parse the request
    @SuppressWarnings("unchecked")
    List<FileItem> fileItems = upload.parseRequest(request);
    Iterator<FileItem> itr = fileItems.iterator();

    while (itr.hasNext()) {
      FileItem fi = itr.next();
      // The following is not being used since in the upload form we are
      // not using any
      // non-file form fields. If you do put in some form fields you want
      // to use,
      // then this is where you will get the values of the form fields. -
      // Sandeep
      if (fi.isFormField()) {
        String name = fi.getFieldName();
        String value = fi.getString();
      }
    }

    String[] fileNames = new String[2];
    itr = fileItems.iterator();
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
    AdminUtils.refreshList(request, getLanguage(request));
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
      boolean success = false;
      StringBuffer messageBuffer = new StringBuffer();
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

  private String processFileItem(FileItem fi) throws FileUploadException {

    // On some browsers fi.getName() will return the full path to the file
    // the client select this can cause problems
    // so the following is a workaround.
    try {
      String fileName = fi.getName();
      if (fileName == null || fileName.trim().length() == 0) {
        return null;
      }
      fileName = FilenameUtils.getName(fileName);

      File fNew = File.createTempFile("opc", ".tmp");
      fNew.deleteOnExit();
      fi.write(fNew);

      File finalFileName = new File(fNew.getParent() + File.separator
          + fileName);
      if (fNew.renameTo(finalFileName)) {
        return finalFileName.getAbsolutePath();
      } else {
        // unable to rename, copy the contents of the file instead
        PortletWarUpdaterUtil.copyFile(fNew, finalFileName, true, false);
        return finalFileName.getAbsolutePath();
      }

    } catch (Exception e) {
      throw new FileUploadException(e.getMessage());
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

  private String getLanguage(HttpServletRequest request) {
    return UserDetail.getCurrentRequester().getUserPreferences().getLanguage();
  }
}
