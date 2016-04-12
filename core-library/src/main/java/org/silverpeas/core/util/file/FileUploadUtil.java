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
package org.silverpeas.core.util.file;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.SilverpeasDiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.UtilException;

/**
 * Utility class for file uploading.
 * @author ehugonnet
 */
public class FileUploadUtil {

  public static final String DEFAULT_ENCODING = CharEncoding.UTF_8;

  private static final ServletFileUpload upload = new ServletFileUpload(
      new SilverpeasDiskFileItemFactory());

  public static boolean isRequestMultipart(HttpServletRequest request) {
    return ServletFileUpload.isMultipartContent(request);
  }

  /**
   * Parses the multipart stream in the specified request to fetch the file items. This method
   * shouldn't be used directly; instead use the HttpRequest instance.
   * @param request the HTTP servlet request.
   * @return a list of file items encoded into the multipart stream of the request.
   * @throws UtilException if an error occurs while fetching the file items.
   */
  public static List<FileItem> parseRequest(HttpServletRequest request)
      throws UtilException {
    try {
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
   * @param encoding the request encoding.
   * @return the parameter value from the list of FileItems. Returns the defaultValue if the
   * parameter is not found.
   */
  public static String getParameter(List<FileItem> items, String parameterName,
      String defaultValue, String encoding) {
    for (FileItem item : items) {
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

  public static List<String> getParameterValues(List<FileItem> items, String parameterName,
      String encoding) {
    List<String> values = new ArrayList<String>();
    for (FileItem item : items) {
      if (item.isFormField() && item.getFieldName().startsWith(parameterName)) {
        try {
          values.add(item.getString(encoding));
        } catch (UnsupportedEncodingException e) {
          values.add(item.getString());
        }
      }
    }
    return values;
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

  @SuppressWarnings("unchecked")
  public static String getOldParameter(List items, String parameterName) {
    return getParameter((List<FileItem>) items, parameterName, null);
  }

  @SuppressWarnings("unchecked")
  public static String getOldParameter(List items, String parameterName, String defaultValue) {
    return getParameter((List<FileItem>) items, parameterName, defaultValue);
  }

  @SuppressWarnings("unchecked")
  public static FileItem getOldFile(List items, String parameterName) {
    return getFile((List<FileItem>) items, parameterName);
  }

  public static FileItem getFile(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  public static FileItem getFile(List<FileItem> items) {
    for (FileItem item : items) {
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
    if (file == null || !StringUtil.isDefined(file.getName())) {
      return "";
    }
    return FileUtil.getFilename(file.getName());
  }

  public static void saveToFile(File file, FileItem item) throws IOException {
    FileUtils.copyInputStreamToFile(item.getInputStream(), file);
  }

  private FileUploadUtil() {
  }
}
