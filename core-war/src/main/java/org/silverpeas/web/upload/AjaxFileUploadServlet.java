/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.silverpeas.core.util.logging.SilverLogger.*;

/**
 * Sample servlet to upload file in an ajax way.
 *
 * @author ehugonnet
 */
public class AjaxFileUploadServlet extends HttpServlet {

  private static final long serialVersionUID = -557782586447656336L;
  private static final String UPLOAD_DIRECTORY = "UPLOAD_DIRECTORY";
  private static final String WHITE_LIST = "WHITE_LIST";
  private static final String FILE_UPLOAD_STATS = "FILE_UPLOAD_STATS";
  private static final String FILE_UPLOAD_PATHS = "FILE_UPLOAD_PATHS";
  private static final String UPLOAD_ERRORS = "UPLOAD_ERRORS";
  private static final String UPLOAD_FATAL_ERROR = "UPLOAD_FATAL_ERROR";
  private static final String SAVING_FILE_FLAG = "SAVING_FILE_FLAG";
  private static String uploadDir;
  private static String whiteList;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    uploadDir = config.getInitParameter(UPLOAD_DIRECTORY);
    whiteList = config.getInitParameter(WHITE_LIST);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();

    if ("status".equals(request.getParameter("q"))) {
      doStatus(session, response);
    } else {
      doFileUpload(session, request);
    }
  }

  /**
   * Do the effective upload of files.
   *
   * @param session the HttpSession
   * @param request the multpart request
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private void doFileUpload(HttpSession session, HttpServletRequest request) throws IOException {
    try {
      session.setAttribute(UPLOAD_ERRORS, "");
      session.setAttribute(UPLOAD_FATAL_ERROR, "");
      List<String> paths = new ArrayList<>();
      session.setAttribute(FILE_UPLOAD_PATHS, paths);
      FileUploadListener listener = new FileUploadListener(request.getContentLength());
      session.setAttribute(FILE_UPLOAD_STATS, listener.getFileUploadStats());
      FileItemFactory factory = new MonitoringFileItemFactory(listener);
      ServletFileUpload upload = new ServletFileUpload(factory);
      List<FileItem> items = upload.parseRequest(request);
      startingToSaveUploadedFile(session);
      String errorMessage = "";
      for (FileItem fileItem : items) {
        if (!fileItem.isFormField() && fileItem.getSize() > 0L) {
          try {
            String filename = fileItem.getName();
            if (filename.indexOf('/') >= 0) {
              filename = filename.substring(filename.lastIndexOf('/') + 1);
            }
            if (filename.indexOf('\\') >= 0) {
              filename = filename.substring(filename.lastIndexOf('\\') + 1);
            }
            if (!isInWhiteList(filename)) {
              errorMessage += "The file " + filename + " is not uploaded!";
              errorMessage += (StringUtil.isDefined(whiteList) ? " Only " + whiteList.
                  replaceAll(" ", ", ") + " file types can be uploaded<br/>"
                  : " No allowed file format has been defined for upload<br/>");
              session.setAttribute(UPLOAD_ERRORS, errorMessage);
            } else {
              filename = System.currentTimeMillis() + "-" + filename;
              File targetDirectory = new File(uploadDir, fileItem.getFieldName());
              targetDirectory.mkdirs();
              File uploadedFile = new File(targetDirectory, filename);
              OutputStream out = null;
              try {
                out = new FileOutputStream(uploadedFile);
                IOUtils.copy(fileItem.getInputStream(), out);
                paths.add(uploadedFile.getParentFile().getName() + '/' + uploadedFile.getName());
              } finally {
                IOUtils.closeQuietly(out);
              }
            }
          } finally {
            fileItem.delete();
          }
        }
      }
    } catch (Exception e) {
      getLogger("upload").warn(e.getMessage());
      session.setAttribute(UPLOAD_FATAL_ERROR,
          "Could not process uploaded file. Please see log for details.");
    } finally {
      endingToSaveUploadedFile(session);
    }
  }

  private synchronized void startingToSaveUploadedFile(HttpSession session) {
    session.setAttribute(SAVING_FILE_FLAG, SAVING_FILE_FLAG);
  }

  private synchronized void endingToSaveUploadedFile(HttpSession session) {
    session.setAttribute(SAVING_FILE_FLAG, null);
  }

  private synchronized boolean isSavingUploadedFile(HttpSession session) {
    return session.getAttribute(SAVING_FILE_FLAG) != null;
  }

  /**
   * Return the current status of the upload.
   *
   * @param session the HttpSession.
   * @param response where the status is to be written.
   * @throws IOException
   */
  private void doStatus(HttpSession session, HttpServletResponse response) throws IOException {
    boolean isSavingUploadedFiles = isSavingUploadedFile(session);
    Long bytesProcessed = null;
    Long totalSize = null;
    FileUploadListener.FileUploadStats fileUploadStats
        = (FileUploadListener.FileUploadStats) session.getAttribute(FILE_UPLOAD_STATS);
    if (fileUploadStats != null) {
      bytesProcessed = fileUploadStats.getBytesRead();
      totalSize = fileUploadStats.getTotalSize();
    }

    // Make sure the status response is not cached by the browser
    response.addHeader("Expires", "0");
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");

    String fatalError = (String) session.getAttribute(UPLOAD_FATAL_ERROR);
    if (StringUtil.isDefined(fatalError)) {
      List<String> paths = (List<String>) session.getAttribute(FILE_UPLOAD_PATHS);
      String uploadedFilePaths = getUploadedFilePaths(paths);
      response.getWriter().println("<b>Upload uncomplete.</b>");
      response.getWriter()
          .println("<script type='text/javascript'>window.parent.stop('" + fatalError + "', "
              + uploadedFilePaths + "); stop('" + fatalError + "', " + uploadedFilePaths
              + ");</script>");
      return;
    }

    if (bytesProcessed != null) {
      long percentComplete = (long) Math.floor((bytesProcessed.doubleValue() / totalSize.
          doubleValue()) * 100.0);
      response.getWriter().println("<b>Upload Status:</b><br/>");

      if (!bytesProcessed.equals(totalSize)) {
        response.getWriter().println(
            "<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: " + percentComplete
            + "%;\"></div></div>");
      } else {
        response.getWriter().println(
            "<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: 100%;"
            + "\"></div></div>");

        if (!isSavingUploadedFiles) {
          List<String> paths = (List<String>) session.getAttribute(FILE_UPLOAD_PATHS);
          String uploadedFilePaths = getUploadedFilePaths(paths);
          String errors = (String) session.getAttribute(UPLOAD_ERRORS);
          if (StringUtil.isDefined(errors)) {
            response.getWriter().println("<b>Upload complete with error(s).</b><br/>");
          } else {
            response.getWriter().println("<b>Upload complete.</b><br/>");
            errors = "";
          }
          response.getWriter()
              .println("<script type='text/javascript'>window.parent.stop('" + errors + "', "
                  + uploadedFilePaths + "); stop('" + errors + "', " + uploadedFilePaths
                  + ");</script>");
        }
      }
    }
  }

  /**
   * Compute a javascript array from the uploaded file paths
   *
   * @param paths les fichiers traités.
   */
  @SuppressWarnings("unchecked")
  private String getUploadedFilePaths(List<String> paths) throws IOException {
    if (paths == null) {
      paths = new ArrayList<>();
    }
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(paths);
  }

  private List<String> getWhiteList() {
    if (StringUtil.isDefined(whiteList)) {
      return Arrays.asList(whiteList.split(" "));
    }
    return Arrays.asList();
  }

  private boolean isInWhiteList(String filename) {
    List<String> whileList = getWhiteList();
    String extension = FilenameUtils.getExtension(filename).toLowerCase();
    return whileList.contains(extension);
  }
}
