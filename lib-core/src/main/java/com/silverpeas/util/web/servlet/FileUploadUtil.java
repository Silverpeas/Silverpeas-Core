/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.stratelia.webactiv.util.exception.UtilException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Utility class for file uploading.
 * @author ehugonnet
 */
public class FileUploadUtil {

  public static final String DEFAULT_ENCODING = "ISO-8859-1";

  public static final boolean isRequestMultipart(HttpServletRequest request) {
    return ServletFileUpload.isMultipartContent(request);
  }

  @SuppressWarnings("unchecked")
  public static final List<FileItem> parseRequest(HttpServletRequest request)
      throws UtilException {
    try {
      // Create a factory for disk-based file items
      FileItemFactory factory = new DiskFileItemFactory();
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      return (List<FileItem>) upload.parseRequest(request);
    } catch (FileUploadException fuex) {
      throw new UtilException("FileUploadUtil.parseRequest",
          "Error uploading files", fuex);
    }
  }

  @SuppressWarnings("unchecked")
  public static final List<FileItem> parseRequest(HttpServletRequest request, int maxSize)
      throws UtilException {
    try { // Create a factory for disk-based file items
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // Set factory constraints
      factory.setSizeThreshold(maxSize);
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      return (List<FileItem>) upload.parseRequest(request);
    } catch (FileUploadException fuex) {
      throw new UtilException("FileUploadUtil.parseRequest",
          "Error uploading files", fuex);
    }
  }

  /**
   * Get the parameter value from the list of FileItems. Returns the defaultValue if the parameter
   * is not found.
   * @param items the items resulting from parsing the request.
   * @param parameterName
   * @param defaultValue the value to be returned if the parameter is not found.
   * @param encoding.
   * @return the parameter value from the list of FileItems. Returns the defaultValue if the
   * parameter is not found.
   */
  public static String getParameter(List<FileItem> items, String parameterName,
      String defaultValue, String encoding) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        try {
          return item.getString(encoding);
        } catch (UnsupportedEncodingException e) {
          return item.getString();
        }
      }
    }
    return defaultValue;
  }

  /**
   * Get the parameter value from the list of FileItems. Returns the defaultValue if the parameter
   * is not found.
   * @param items the items resulting from parsing the request.
   * @param parameterName
   * @param defaultValue the value to be returned if the parameter is not found.
   * @return the parameter value from the list of FileItems. Returns the defaultValue if the
   * parameter is not found.
   */
  public static String getParameter(List<FileItem> items, String parameterName, String defaultValue) {
    return getParameter(items, parameterName, defaultValue, DEFAULT_ENCODING);
  }

  /**
   * Get the parameter value from the list of FileItems. Returns null if the parameter is not found.
   * @param items the items resulting from parsing the request.
   * @param parameterName
   * @return the parameter value from the list of FileItems. Returns null if the parameter is not
   * found.
   */
  public static String getParameter(List<FileItem> items, String parameterName) {
    return getParameter(items, parameterName, null);
  }

  public static String getOldParameter(List items, String parameterName) {
    return getParameter((List<FileItem>) items, parameterName, null);
  }

  public static String getOldParameter(List items, String parameterName, String defaultValue) {
    return getParameter((List<FileItem>) items, parameterName, defaultValue);
  }

  public static FileItem getOldFile(List items, String parameterName) {
    return getFile((List<FileItem>) items, parameterName);
  }

  public static FileItem getFile(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  public static FileItem getFile(List<FileItem> items) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField()) {
        return item;
      }
    }
    return null;
  }

  public static FileItem getFile(HttpServletRequest request) throws UtilException {
    List<FileItem> items = FileUploadUtil.parseRequest(request);
    return FileUploadUtil.getFile(items);
  }

  public static String getFileName(FileItem file) {
    if (file == null) {
      return "";
    }
    String fullFileName = file.getName();
    if (fullFileName == null) {
      return "";
    }
    return fullFileName.substring(fullFileName.lastIndexOf(File.separator) + 1, fullFileName
        .length());
  }

  public static void saveToFile(File file, FileItem item) throws IOException {
    OutputStream out = FileUtils.openOutputStream(file);
    InputStream in = item.getInputStream();
    try {
      IOUtils.copy(in, out);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
