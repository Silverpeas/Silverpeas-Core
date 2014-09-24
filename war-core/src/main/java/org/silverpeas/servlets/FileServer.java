/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.ResourceLocator;
import com.stratelia.webactiv.statistic.control.StatisticBm;
import org.silverpeas.file.SilverpeasFile;
import org.silverpeas.file.SilverpeasFileDescriptor;
import org.silverpeas.file.SilverpeasFileProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

import static org.silverpeas.util.FileServerUtils.*;

public class FileServer extends AbstractFileSender {

  private static final long serialVersionUID = 6377810839728682983L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Method declaration
   *
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("peasUtil", "FileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
    String componentId = req.getParameter(COMPONENT_ID_PARAMETER);

    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if ((mainSessionCtrl == null) || (!isUserAllowed(mainSessionCtrl, componentId))) {
      SilverTrace.warn("peasUtil", "FileServer.doPost", "root.MSG_GEN_SESSION_TIMEOUT",
          "NewSessionId=" + session.getId() + URLManager.getApplicationURL() +
              GeneralPropertiesManager.getString("sessionTimeout"));
      res.sendRedirect(
          URLManager.getApplicationURL() + GeneralPropertiesManager.getString("sessionTimeout"));
      return;
    }

    String mimeType = req.getParameter(MIME_TYPE_PARAMETER);
    String sourceFile = req.getParameter(SOURCE_FILE_PARAMETER);
    String archiveIt = req.getParameter(ARCHIVE_IT_PARAMETER);
    String dirType = req.getParameter(DIR_TYPE_PARAMETER);
    String userId = req.getParameter(USER_ID_PARAMETER);
    String typeUpload = req.getParameter(TYPE_UPLOAD_PARAMETER);
    String size = req.getParameter(SIZE_PARAMETER);

    if (StringUtil.isDefined(size)) {
      sourceFile = size + File.separatorChar + sourceFile;
    }

    SilverpeasFileDescriptor descriptor =
        new SilverpeasFileDescriptor(componentId).fileName(sourceFile).mimeType(mimeType);
    if (typeUpload != null) {
      descriptor.absolutePath();
    } else {
      if (dirType != null) {
        if (dirType.equals(GeneralPropertiesManager.getString("RepositoryTypeTemp"))) {
          descriptor = descriptor.temporaryFile();
        }
      } else {
        String directory = req.getParameter(DIRECTORY_PARAMETER);
        descriptor = descriptor.parentDirectory(directory);
      }
    }
    SilverpeasFile file = SilverpeasFileProvider.getFile(descriptor);
    sendFile(res, file);

    if (StringUtil.isDefined(archiveIt)) {
      String nodeId = req.getParameter(NODE_ID_PARAMETER);
      String pubId = req.getParameter(PUBLICATION_ID_PARAMETER);
      ForeignPK pubPK = new ForeignPK(pubId, componentId);
      try {
        StatisticBm statisticBm = EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
            StatisticBm.class);
        statisticBm.addStat(userId, pubPK, 1, "Publication");
      } catch (Exception ex) {
        SilverTrace.warn("peasUtil", "FileServer.doPost", "peasUtil.CANNOT_WRITE_STATISTICS",
            "pubPK = " + pubPK + " and nodeId = " + nodeId, ex);
      }
    }
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    boolean isAllowed;
    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      if ("yes"
          .equalsIgnoreCase(controller.getComponentParameterValue(componentId, "publicFiles"))) {
        // Case of file contained in a component used as a file storage
        isAllowed = true;
      } else {
        isAllowed = controller.getOrganisationController()
            .isComponentAvailable(componentId, controller.getUserId());
      }
    }
    return isAllowed;
  }

  @Override
  protected ResourceLocator getResources() {
    return new ResourceLocator("org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");
  }
}
