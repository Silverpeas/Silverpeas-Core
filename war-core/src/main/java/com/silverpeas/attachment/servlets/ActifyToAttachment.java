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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.attachment.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Class declaration
 * @author
 */
public class ActifyToAttachment extends HttpServlet {

  private static final long serialVersionUID = -1903790800260389933L;

  /**
   * Method declaration
   * @param config
   * @see
   */
  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("attachment", "ActifyToSilverpeas.init",
          "attachment.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  /**
   * Method declaration
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Method declaration
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("attachment", "ActifyToAttachment.doPost", "root.MSG_GEN_ENTER_METHOD");
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    if (settings.getBoolean("ActifyPublisherEnable", false)) {
      try {
        SilverTrace.info("attachment", "ActifyToAttachment.doPost", "req=" + req.getParameter(
            "AttachmentId"));
        int size = Integer.parseInt(req.getParameter("FileSize"));
        String attachmentId = req.getParameter("AttachmentId");
        String logicalName = req.getParameter("LogicalName");
        boolean indexIt = false;
        // Get AttachmentDetail Object
        AttachmentDetail ad = AttachmentController.searchAttachmentByPK(new AttachmentPK(
            attachmentId));
        if (ad != null) {
          // Remove string "HIDDEN" from the beginning of the instanceId
          String instanceId = ad.getInstanceId().substring(7);
          ad.setInstanceId(instanceId);
          ad.setSize(size);
          ad.setLogicalName(logicalName);
          AttachmentController.updateAttachment(ad, indexIt);
          // Copy 3D file converted from Actify Work directory to Silverpeas workspaces
          String actifyWorkingPath = "Actify";
          String srcFile = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath
              + File.separator + logicalName;
          String destFile = AttachmentController.createPath(ad.getInstanceId(), null)
              + ad.getPhysicalName();

          SilverTrace.info("attachment", "ActifyToAttachment", "root.MSG_GEN_PARAM_VALUE",
              "srcFile = " + srcFile);
          SilverTrace.info("attachment", "ActifyToAttachment", "root.MSG_GEN_PARAM_VALUE",
              "destFile = " + destFile);
          FileRepositoryManager.copyFile(srcFile, destFile);
        }
      } catch (Exception e) {
        SilverTrace.error("attachment", "ActifyToAttachment.doPost", "ERREUR", e);
      }
    }
  }
}