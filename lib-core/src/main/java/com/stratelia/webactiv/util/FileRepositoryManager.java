/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.silverpeas.search.indexEngine.IndexFileManager;
import static java.io.File.separatorChar;

/**
 * @author Norbert CHAIX
 * @version
 */
public class FileRepositoryManager {

  static final String exportTemplatePath = GeneralPropertiesManager.getString("exportTemplatePath");
  final static String upLoadPath = GeneralPropertiesManager.getString("uploadsPath");
  final static String avatarPath = GeneralPropertiesManager.getString("avatar.path", upLoadPath
      + File.separatorChar + "avatar");
  static String tempPath = "";
  static String domainPropertiesFolderPath;
  static String domainAuthenticationPropertiesFolderPath;
  final static ResourceLocator uploadSettings = new ResourceLocator(
      "org.silverpeas.util.uploads.uploadSettings", "");
  static final ResourceLocator utilMessages = new ResourceLocator(
      "org.silverpeas.util.multilang.util", "");
  static final String unknownFileIcon = uploadSettings.getString("unknown");
  public static final String CONTEXT_TOKEN = ",";
  static final long ko = 1024;
  static final long mo = ko * 1024;
  static final long go = mo * 1024;
  static final long to = go * 1024;

  static {
    tempPath = GeneralPropertiesManager.getString("tempPath");
    if (!tempPath.endsWith(File.separator)) {
      tempPath = tempPath + File.separatorChar;
    }

    StringBuilder path = new StringBuilder();
    path.append(System.getenv("SILVERPEAS_HOME")).append(separatorChar).append("properties");
    path.append(separatorChar).append("org").append(separatorChar).append("silverpeas").append(
        separatorChar);

    domainPropertiesFolderPath = path.toString() + "domains" + separatorChar;
    domainAuthenticationPropertiesFolderPath = path.toString() + "authentication" + separatorChar;
  }

  /**
   * @deprecated @param sSpaceId
   * @param sComponentId
   * @return
   */
  public static String getAbsolutePath(String sSpaceId, String sComponentId) {
    SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
        "concat: sSpaceId = " + sSpaceId + " sComponentId= " + sComponentId);
    return upLoadPath + File.separator + sComponentId + File.separator;
  }

  public static String getAbsolutePath(String sComponentId) {
    SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
        " sComponentId= " + sComponentId);
    return upLoadPath + File.separator + sComponentId + File.separator;
  }

  public static String getAvatarPath() {
    return avatarPath;
  }

  /**
   * Gets the path of the repository into which attachments and other files are uploaded in
   * Silverpeas.
   *
   * @return the path of the root repository for uploads.
   */
  public static String getUploadPath() {
    return upLoadPath + File.separator;
  }

  // Add by Jean-Claude Groccia
  // @param: sSpaceId: type String: the name of the space
  // @param: sComponentId: type String: the name of the componentId
  // @param: sDirectoryName: type Array of String: the name of sub directory.
  // this parameter represents the context of component
  /**
   * @deprecated
   */
  public static String getAbsolutePath(String sSpaceId, String sComponentId,
      String[] sDirectoryName) {
    return getAbsolutePath(sComponentId, sDirectoryName);
  }

  /**
   * @param sSpaceId
   * @param sComponentId
   * @param sDirectoryName
   * @return path
   */
  public static String getAbsolutePath(String sComponentId, String[] sDirectoryName) {
    int lg = sDirectoryName.length;
    String path = getAbsolutePath(sComponentId);
    for (int k = 0; k < lg; k++) {
      SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
          ("concat: path = " + path + " sDirectoryName[" + k + "]=" + sDirectoryName[k]));
      path = path + sDirectoryName[k] + File.separatorChar;
    }
    return path;
  }

  // Add by Jean-Claude Groccia
  // @param: sDirectoryName: type of String: the name of sub directory.
  // @return path1/path2/
  public static String getRelativePath(String[] sDirectoryName) {
    int lg = sDirectoryName.length;
    String path = sDirectoryName[0];
    path = path.concat(File.separator);
    for (int k = 1; k < lg; k++) {
      SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
          "concat: path = " + path + " sDirectoryName[" + k + "]="
          + sDirectoryName[k]);
      path = path.concat(sDirectoryName[k]);
      path = path.concat(File.separator);
    }
    return path;
  }

  public static String getDomainPropertiesPath(String domainName) {
    return domainPropertiesFolderPath + "domain" + domainName + ".properties";
  }

  public static String getDomainAuthenticationPropertiesPath(String domainName) {
    return domainAuthenticationPropertiesFolderPath + "autDomain" + domainName + ".properties";
  }

  public static String getTemporaryPath() {
    return tempPath + File.separator;
  }

  public static String getTemporaryPath(String sSpaceId, String sComponentId) {
    return tempPath + File.separator;
  }

  public static String getComponentTemporaryPath(String sComponentId) {
    return getAbsolutePath(sComponentId) + "Temp" + File.separator;
  }

  /**
   * @param sSpaceId
   * @param sComponentId
   * @param sDirectoryName
   * @deprecated
   */
  public static void createAbsolutePath(String sSpaceId, String sComponentId,
      String sDirectoryName) {
    FileFolderManager.createFolder(getAbsolutePath(sComponentId)
        + sDirectoryName);
  }

  public static void createAbsolutePath(String sComponentId, String sDirectoryName) {
    FileFolderManager.createFolder(getAbsolutePath(sComponentId)
        + sDirectoryName);
  }

  /**
   * @param spaceId
   * @param componentId
   * @param directoryName
   * @deprecated
   */
  public static void createTempPath(String spaceId, String componentId, String directoryName) {
    FileFolderManager.createFolder(getAbsolutePath(componentId));
  }

  public static void createTempPath(String componentId, String directoryName) {
    FileFolderManager.createFolder(getAbsolutePath(componentId));
  }

  public static void createGlobalTempPath(String directoryName) {
    FileFolderManager.createFolder(getTemporaryPath() + directoryName);
  }

  public static void createAbsoluteIndexPath(String particularSpace, String componentId) {
    FileFolderManager.createFolder(IndexFileManager.getAbsoluteIndexPath(particularSpace,
        componentId));
  }

  public static void deleteAbsolutePath(String sSpaceId, String sComponentId,
      String sDirectoryName) {
    FileFolderManager.deleteFolder(getAbsolutePath(sComponentId)
        + sDirectoryName);
  }

  public static void deleteTempPath(String sSpaceId, String sComponentId,
      String sDirectoryName) {
    FileFolderManager.deleteFolder(getAbsolutePath(sComponentId));
  }

  public static void deleteAbsoluteIndexPath(String particularSpace,
      String sComponentId) {
    FileFolderManager.deleteFolder(IndexFileManager.getAbsoluteIndexPath(particularSpace,
        sComponentId));
  }

  public static String getFileIcon(boolean small, String extension) {
    return getFileIcon(small, extension, false);
  }

  public static String getFileIcon(String extension) {
    return getFileIcon(false, extension);
  }

  /**
   * Get File icon
   *
   * @param extension
   * @param isReadOnly
   * @return
   */
  public static String getFileIcon(String extension, boolean isReadOnly) {
    return getFileIcon(false, extension, isReadOnly);
  }

  public static String getFileIcon(boolean small, String extension, boolean isReadOnly) {
    String path = URLManager.getApplicationURL() + uploadSettings.getString("FileIconsPath");

    String fileIcon = uploadSettings.getString(extension.toLowerCase());
    if (fileIcon == null) {
      fileIcon = unknownFileIcon;
    } else {
      if (isReadOnly) {
        fileIcon = fileIcon.substring(0, fileIcon.lastIndexOf(".gif")) + "Lock.gif";
      }
    }
    if (small && fileIcon != null) {
      String newFileIcon = fileIcon.substring(0, fileIcon.lastIndexOf(".gif")) + "Small.gif";
      fileIcon = newFileIcon;
    }

    return path + fileIcon;
  }

  public static String getFileExtension(String fileName) {
    return FilenameUtils.getExtension(fileName);
  }

  /**
   * Get the file size with the suitable unit
   *
   * @param lSize : size
   * @return String
   */
  public static String formatFileSize(long lSize) {
    String sTo = utilMessages.getString("to", "Tb");
    String sGo = utilMessages.getString("go", "Gb");
    String sMo = utilMessages.getString("mo", "Mb");
    String sKo = utilMessages.getString("ko", "Kb");
    String o = utilMessages.getString("o", "bytes");

    float size = new Long(lSize).floatValue();

    if (size < ko) {// inférieur à 1 ko (1024 octets)
      return Integer.toString(Math.round(size)).concat(" ").concat(o);
    } else if (size < mo) {// inférieur à 1 mo (1024 * 1024 octets)
      return Integer.toString(Math.round(size / ko)).concat(" ").concat(sKo);
    } else if (size < go) {// inférieur à 1 Go
      DecimalFormat format = new DecimalFormat();
      format.setMaximumFractionDigits(2);
      return format.format(size / mo).concat(" ").concat(sMo);
    } else if (size < to) {// inférieur à 1 To
      DecimalFormat format = new DecimalFormat();
      format.setMaximumFractionDigits(2);
      return format.format(size / go).concat(" ").concat(sGo);
    } else {
      DecimalFormat format = new DecimalFormat();
      format.setMaximumFractionDigits(2);
      return format.format(size / to).concat(" ").concat(sTo);
    }
  }

  /**
   * Get the size of a file (in bytes)
   *
   * @param sourceFile
   * @return int
   */
  public static long getFileSize(String sourceFile) {
    return new File(sourceFile).length();
  }

  /**
   * Get the estimated download time
   *
   * @param size the file's size
   * @return String
   */
  public static String getFileDownloadTime(long size) {
    int fileSizeReference = Integer.parseInt(uploadSettings.getString("FileSizeReference"));
    int theoricDownloadTime = Integer.parseInt(uploadSettings.getString("DownloadTime"));
    long fileDownloadEstimation = ((size * theoricDownloadTime) / fileSizeReference) / 60;
    if (fileDownloadEstimation < 1) {
      return "t < 1 min";
    }
    if ((fileDownloadEstimation >= 1) && (fileDownloadEstimation < 5)) {
      return "1 < t < 5 mins";
    }
    return " t > 5 mins";
  }

  /**
   * Copy a contents from a file to another one
   *
   * @author Seb
   * @param from The name of the source file, the one to copy.
   * @param to The name of the destination file, where to paste data.
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void copyFile(String from, String to) throws IOException {
    FileUtils.copyFile(new File(from), new File(to));
  }

  public static String formatFileUploadTime(long size) {
    String min = " m";
    String sec = " s";
    String ms = " ms";
    if (size < 1000) {
      return Long.toString(size) + ms;
    } else if (size < 120000) {
      return Long.toString(size / 1000) + sec;
    } else {
      return Long.toString(size / 60000) + min;
    }
  }

  /**
   * to create the array of the string this array represents the repertories where the files must be
   * stored.
   *
   * @param str : type String: the string of repertories
   */
  public static String[] getAttachmentContext(String str) {

    String strAt = "Attachment " + CONTEXT_TOKEN;

    if (str != null) {
      strAt = strAt.concat(str);
    }
    StringTokenizer strToken = new StringTokenizer(strAt, CONTEXT_TOKEN);
    // number of elements
    int nElt = strToken.countTokens();
    // to init array
    String[] context = new String[nElt];

    int k = 0;

    while (strToken.hasMoreElements()) {
      context[k] = ((String) strToken.nextElement()).trim();
      k++;
    }
    return context;
  }

  /**
   * Gets the path of the repository that contains the templates to use in exports.
   *
   * @return the path of the export template repository.
   */
  public static String getExportTemplateRepository() {
    String path = exportTemplatePath;
    if (!path.endsWith("/")) {
      path += "/";
    }
    return path;
  }
}
