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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.file;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file uploading.
 * @author ehugonnet
 */
public class FileUploadUtil {

  public static final String DEFAULT_ENCODING = Charsets.UTF_8.name();

  private FileUploadUtil() {
  }

  private static final ServletFileUpload upload = new ServletFileUpload(
      new DiskFileItemFactoryProvider().provide());

  public static boolean isRequestMultipart(HttpServletRequest request) {
    return ServletFileUpload.isMultipartContent(request);
  }

  /**
   * Parses the multipart stream in the specified request to fetch the file items. This method
   * shouldn't be used directly; instead use the HttpRequest instance.
   * @param request the HTTP servlet request.
   * @return a list of file items encoded into the multipart stream of the request.
   * @throws SilverpeasRuntimeException if an error occurs while fetching the file items.
   */
  public static List<FileItem> parseRequest(HttpServletRequest request) {
    try {
      // Parse the request
      return upload.parseRequest(request);
    } catch (FileUploadException e) {
      throw new SilverpeasRuntimeException("Error uploading files", e);
    }
  }

  /**
   * Get the parameter value from the list of FileItems. Returns the defaultValue if the parameter
   * is not found.
   * @param items the items resulting from parsing the request.
   * @param parameterName name of the parameter.
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
    List<String> values = new ArrayList<>();
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
   * @param parameterName the name of the parameter.
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
   * @param parameterName the name of the parameter.
   * @return the parameter value from the list of FileItems. Returns null if the parameter is not
   * found.
   */
  public static String getParameter(List<FileItem> items, String parameterName) {
    return getParameter(items, parameterName, null);
  }

  public static String getOldParameter(List<FileItem> items, String parameterName) {
    return getParameter(items, parameterName, null);
  }

  public static String getOldParameter(List<FileItem> items, String parameterName, String defaultValue) {
    return getParameter(items, parameterName, defaultValue);
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

  public static FileItem getFile(HttpServletRequest request) {
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
}
