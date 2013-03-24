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

package org.silverpeas.attachment.web;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Class declaration
 * @author
 */
public class ActifyToAttachment extends HttpServlet {

  private static final long serialVersionUID = -1903790800260389933L;

  /**
   * Method declaration
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("attachment", "ActifyToAttachment.doPost", "root.MSG_GEN_ENTER_METHOD");
    ResourceLocator settings = new ResourceLocator("org.silverpeas.util.attachment.Attachment", "");
    if (settings.getBoolean("ActifyPublisherEnable", false)) {
      try {
        SilverTrace.info("attachment", "ActifyToAttachment.doPost", "req=" + req.getParameter(
            "AttachmentId"));
        long size = Long.parseLong(req.getParameter("FileSize"));
        String attachmentId = req.getParameter("AttachmentId");
        String logicalName = req.getParameter("LogicalName");
        boolean indexIt = false;
        // Get AttachmentDetail Object
        SimpleDocument ad = AttachmentServiceFactory.getAttachmentService().searchDocumentById(new
            SimpleDocumentPK(attachmentId), null);
        if (ad != null) {
          // Remove string "HIDDEN" from the beginning of the instanceId
          String instanceId = ad.getInstanceId().substring(7);
          ad.getPk().setComponentName(instanceId);
          ad.setSize(size);
          ad.setFilename(logicalName);
          AttachmentServiceFactory.getAttachmentService().updateAttachment(ad, indexIt, false);
          // Copy 3D file converted from Actify Work directory to Silverpeas workspaces
          String actifyWorkingPath = "Actify";
          String srcFile = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath
              + File.separator + logicalName;
          String destFile = ad.getAttachmentPath();
          SilverTrace.info("attachment", "ActifyToAttachment", "root.MSG_GEN_PARAM_VALUE",
              "srcFile = " + srcFile + " and destFile = " + destFile);
          FileRepositoryManager.copyFile(srcFile, destFile);
        }
      } catch (Exception e) {
        SilverTrace.error("attachment", "ActifyToAttachment.doPost", "ERREUR", e);
      }
    }
  }
}