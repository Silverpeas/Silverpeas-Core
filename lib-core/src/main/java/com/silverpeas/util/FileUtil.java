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
package com.silverpeas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileUtil implements MimeTypes {

  private static ResourceLocator MIME_TYPES_EXTENSIONS = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.mime_types", "");

  public static final String CONTEXT_TOKEN = ",";

  public static final String BASE_CONTEXT = "Attachment";

  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  /**
   * Extract the mime-type from the file name.
   * @param fileName the name of the file.
   * @return the mime-type as a String.
   */
  public static String getMimeType(String fileName) {
    String mimeType = null;
    String fileExtension = FileRepositoryManager.getFileExtension(fileName)
        .toLowerCase();
    try {
      if (MIME_TYPES_EXTENSIONS != null) {
        mimeType = MIME_TYPES_EXTENSIONS.getString(fileExtension);
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("attachment", "AttachmentController",
          "attachment.MSG_MISSING_MIME_TYPES_PROPERTIES", null, e);
    } catch (NullPointerException e) {
      SilverTrace.warn("attachment", "AttachmentController",
          "attachment.MSG_FILE_LOGICAL_NAME_NULL", null, e);
    }
    if (mimeType == null) {
      MIME_TYPES.getContentType(fileName);
    }
    if (ARCHIVE_MIME_TYPE.equalsIgnoreCase(mimeType)) {
      if (JAR_EXTENSION.equalsIgnoreCase(fileExtension)
          || WAR_EXTENSION.equalsIgnoreCase(fileExtension)
          || EAR_EXTENSION.equalsIgnoreCase(fileExtension)) {
        mimeType = JAVA_ARCHIVE_MIME_TYPE;
      } else if ("3D".equalsIgnoreCase(fileExtension)) {
        mimeType = SPINFIRE_MIME_TYPE;
      }
    }
    if (mimeType == null) {
      mimeType = DEFAULT_MIME_TYPE;
    }
    return mimeType;
  }

  /**
   * to create the array of the string this array represents the repertories where the files must be
   * stored.
   * @param str : type String: the string of repertories
   * @param token : type String: the token separating the repertories
   */
  public static String[] getAttachmentContext(String context) {
    if (!StringUtil.isDefined(context)) {
      return new String[] { BASE_CONTEXT };
    }
    StringTokenizer strToken = new StringTokenizer(context, CONTEXT_TOKEN);
    List<String> folders = new ArrayList<String>(10);
    folders.add(BASE_CONTEXT);
    while (strToken.hasMoreElements()) {
      folders.add(strToken.nextToken().trim());
    }
    return folders.toArray(new String[folders.size()]);
  }

  /**
   * Read the content of a file in a byte array.
   * @param file the file to be read.
   * @return the bytes array containing the content of the file.
   * @throws IOException
   */
  public static byte[] readFile(File file) throws IOException {
    return FileUtils.readFileToByteArray(file);
  }

  /**
   * Write a stream into a file.
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(File file, InputStream data) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      IOUtils.copy(data, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Write a stream into a file.
   * @param file the file to be written.
   * @param data the data to be written.
   * @throws IOException
   */
  public static void writeFile(File file, Reader data) throws IOException {
    FileWriter out = null;
    BufferedReader in = null;
    try {
      out = new FileWriter(file);
      IOUtils.copy(data, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Loads a ResourceBundle from the Silverpeas configuration directory.
   * @param name the name of the bundle.
   * @param locale the locale of the bundle.
   * @return the corresponding ResourceBundle if it exists - null otherwise.
   */
  public static ResourceBundle loadBundle(String name, Locale locale) {
    Locale loc = locale;
    if (loc == null) {
      loc = Locale.ROOT;
    }
    return ResourceBundle.getBundle(name, loc, new ConfigurationClassLoader(FileUtil.class
        .getClassLoader()));
  }

  /**
   * Indicates if the OS is from the Microsoft Windows familly
   * @return true if the OS is from the Microsoft Windows familly - false otherwise.
   */
  public static boolean isWindows() {
    return OsEnum.getOS().isWindows();
  }
}
