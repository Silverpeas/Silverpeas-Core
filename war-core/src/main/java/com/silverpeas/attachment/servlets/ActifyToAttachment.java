package com.silverpeas.attachment.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ActifyToAttachment extends HttpServlet {
  HttpSession session;
  PrintWriter out;

  /**
   * Method declaration
   * 
   * 
   * @param config
   * 
   * @see
   */
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
   * 
   * 
   * @param req
   * @param res
   * 
   * @throws IOException
   * @throws ServletException
   * 
   * @see
   */
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Method declaration
   * 
   * 
   * @param req
   * @param res
   * 
   * @throws IOException
   * @throws ServletException
   * 
   * @see
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("attachment", "ActifyToAttachment.doPost",
        "root.MSG_GEN_ENTER_METHOD");
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    if (settings.getBoolean("ActifyPublisherEnable", false)) {
      try {
        SilverTrace.info("attachment", "ActifyToAttachment.doPost", "req="
            + req.getParameter("AttachmentId"));

        int size = Integer.parseInt(req.getParameter("FileSize"));
        String attachmentId = req.getParameter("AttachmentId");
        String logicalName = req.getParameter("LogicalName");

        boolean indexIt = false;

        // Get AttachmentDetail Object
        AttachmentDetail ad = AttachmentController
            .searchAttachmentByPK(new AttachmentPK(attachmentId));
        if (ad != null) {
          // Remove string "HIDDEN" from the beginning of the instanceId
          String instanceId = ad.getInstanceId().substring(7);
          ad.setInstanceId(instanceId);

          ad.setSize(size);
          ad.setLogicalName(logicalName);

          AttachmentController.updateAttachment(ad, indexIt);

          // Copy 3D file converted from Actify Work directory to Silverpeas
          // workspaces
          String actifyWorkingPath = "Actify";
          String srcFile = FileRepositoryManager.getTemporaryPath()
              + actifyWorkingPath + File.separator + logicalName;
          String destFile = AttachmentController.createPath(ad.getInstanceId(),
              null)
              + ad.getPhysicalName();

          SilverTrace.info("attachment", "ActifyToAttachment",
              "root.MSG_GEN_PARAM_VALUE", "srcFile = " + srcFile);
          SilverTrace.info("attachment", "ActifyToAttachment",
              "root.MSG_GEN_PARAM_VALUE", "destFile = " + destFile);

          FileRepositoryManager.copyFile(srcFile, destFile);
        }
      } catch (Exception e) {
        SilverTrace.error("attachment", "ActifyToAttachment.doPost", "ERREUR",
            e);
      }
    }
  }
}