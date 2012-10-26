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

package com.stratelia.webactiv.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import org.apache.commons.io.IOUtils;

/**
 * Class declaration
 * @author
 */
public class OnlineFileServer extends HttpServlet {

  private static final long serialVersionUID = -6153872618631360113L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("peasUtil", "FileServer.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("peasUtil", "OnlineFileServer.doPost",
        "root.MSG_GEN_ENTER_METHOD");
    String mimeType = req.getParameter("MimeType");
    String sourceFile = req.getParameter("SourceFile");
    String directory = req.getParameter("Directory");
    String componentId = req.getParameter("ComponentId");

    String attachmentId = req.getParameter("attachmentId");
    String language = req.getParameter("lang");
    AttachmentDetail attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      // Check first if attachment exists
      attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
          attachmentId));
      if (attachment != null) {
        mimeType = attachment.getType(language);
        sourceFile = attachment.getPhysicalName(language);
        directory = FileRepositoryManager.getRelativePath(FileRepositoryManager
            .getAttachmentContext(attachment.getContext()));
      }
    }

    String documentId = req.getParameter("DocumentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = req.getParameter("VersionId");
      VersioningUtil versioning = new VersioningUtil();
      DocumentVersionPK versionPK =
          new DocumentVersionPK(Integer.parseInt(versionId), "useless", componentId);
      DocumentVersion version = versioning.getDocumentVersion(versionPK);

      if (version != null) {
        mimeType = version.getMimeType();
        sourceFile = version.getPhysicalName();

        String[] path = new String[1];
        path[0] = "Versioning";
        directory = FileRepositoryManager.getRelativePath(path);
      }
    }

    String filePath =
        FileRepositoryManager.getAbsolutePath(componentId) + directory + File.separator +
        sourceFile;
    res.setContentType(mimeType);
    display(res, filePath);
  }

  /**
   * This method writes the result of the preview action.
   * @param res - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   * this String is null that an exception had been catched the html document generated is empty !!
   * also, we display a warning html page
   */
  private void display(HttpServletResponse res, String htmlFilePath) throws IOException {
    OutputStream out2 = res.getOutputStream();
    int read;
    BufferedInputStream input = null; // for the html document generated
    SilverTrace.info("peasUtil", "OnlineFileServer.display()",
        "root.MSG_GEN_ENTER_METHOD", " htmlFilePath " + htmlFilePath);
    try {
      input = new BufferedInputStream(new FileInputStream(htmlFilePath));
      read = input.read();
      SilverTrace.info("peasUtil", "OnlineFileServer.display()",
          "root.MSG_GEN_ENTER_METHOD", " BufferedInputStream read " + read);
      if (read == -1) {
        displayWarningHtmlCode(res);
      } else {
        while (read != -1) {
          out2.write(read); // writes bytes into the response
          read = input.read();
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.doPost",
          "root.EX_CANT_READ_FILE", "file name=" + htmlFilePath);
      displayWarningHtmlCode(res);
    } finally {
      SilverTrace.info("peasUtil", "OnlineFileServer.display()", "",
          " finally ");
      // we must close the in and out streams
      try {
        if (input != null) {
          input.close();
        }
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "OnlineFileServer.display",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }

  // Add By Mohammed Hguig

  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    StringReader message = null;
    OutputStream output = res.getOutputStream();
    ResourceLocator resourceLocator = new ResourceLocator(
        "com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");
    message = new StringReader(resourceLocator.getString("warning"));
    try {
      IOUtils.copy(message, output);
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      IOUtils.closeQuietly(output);
      IOUtils.closeQuietly(message);
    }
  }

}
