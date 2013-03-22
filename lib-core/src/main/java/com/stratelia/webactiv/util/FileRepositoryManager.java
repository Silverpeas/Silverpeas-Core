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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.silverpeas.search.indexEngine.IndexFileManager;
import org.silverpeas.util.UnitUtil;

import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

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
  static final String unknownFileIcon = uploadSettings.getString("unknown");
  public static final String CONTEXT_TOKEN = ",";

  static {
    tempPath = GeneralPropertiesManager.getString("tempPath");
    if (!tempPath.endsWith(File.separator)) {
      tempPath = tempPath + File.separatorChar;
    }

    StringBuilder path = new StringBuilder(512);
    path.append(System.getenv("SILVERPEAS_HOME")).append(separatorChar).append("properties");
    path.append(separatorChar).append("org").append(separatorChar).append("silverpeas").append(
        separatorChar);

    domainPropertiesFolderPath = path.toString() + "domains" + separatorChar;
    domainAuthenticationPropertiesFolderPath = path.toString() + "authentication" + separatorChar;
  }

  /**
   *
   * @param sSpaceId
   * @param sComponentId
   * @return
   * @deprecated
   */
  @Deprecated
  public static String getAbsolutePath(String sSpaceId, String sComponentId) {
    SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
        "concat: sSpaceId = " + sSpaceId + " sComponentId= " + sComponentId);
    return upLoadPath + separatorChar + sComponentId + separatorChar;
  }

  public static String getAbsolutePath(String sComponentId) {
    SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
        " sComponentId= " + sComponentId);
    return upLoadPath + separatorChar + sComponentId + separatorChar;
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
    return upLoadPath + separatorChar;
  }

  /**
   * Add by Jean-Claude Groccia
   *
   * @param: spaceId: type String: the name of the space
   * @param: componentId: type String: the name of the componentId
   * @param: directoryName: type Array of String: the name of sub directory. this parameter
   * represents the context of component
   * @deprecated
   */
  @Deprecated
  public static String getAbsolutePath(String spaceId, String componentId, String[] directoryName) {
    return getAbsolutePath(componentId, directoryName);
  }

  /**
   * @param componentId
   * @param directoryName
   * @return path
   */
  public static String getAbsolutePath(String componentId, String[] directoryName) {
    int lg = directoryName.length;
    String path = getAbsolutePath(componentId);
    for (int k = 0; k < lg; k++) {
      SilverTrace.debug("util", "FileRepositoryManager.getAbsolutePath",
          ("concat: path = " + path + " sDirectoryName[" + k + "]=" + directoryName[k]));
      path = path + directoryName[k] + separatorChar;
    }
    return path;
  }

  /**
   * Construct an OS specific relative path.
   *
   * @param directories the names of sub directory. (path1, path2,...)
   * @return path1/path2/.../
   */
  public static String getRelativePath(String... directories) {
    return StringUtil.join(directories, separatorChar) + separatorChar;
  }

  public static String getTemporaryPath() {
    return tempPath + separatorChar;
  }

  public static String getDomainPropertiesPath(String domainName) {
    return domainPropertiesFolderPath + "domain" + domainName + ".properties";
  }

  public static String getDomainAuthenticationPropertiesPath(String domainName) {
    return domainAuthenticationPropertiesFolderPath + "autDomain" + domainName + ".properties";
  }

  public static String getTemporaryPath(String sSpaceId, String sComponentId) {
    return tempPath + separatorChar;
  }

  public static String getComponentTemporaryPath(String sComponentId) {
    return getAbsolutePath(sComponentId) + "Temp" + separatorChar;
  }

  /**
   * @param spaceId
   * @param componentId
   * @param directoryName
   * @deprecated
   */
  @Deprecated
  public static void createAbsolutePath(String spaceId, String componentId, String directoryName) {
    FileFolderManager.createFolder(getAbsolutePath(componentId) + directoryName);
  }

  public static void createAbsolutePath(String componentId, String directoryName) {
    FileFolderManager.createFolder(getAbsolutePath(componentId) + directoryName);
  }

  /**
   * @param spaceId
   * @param componentId
   * @param directoryName
   * @deprecated
   */
  @Deprecated
  public static void createTempPath(String spaceId, String componentId, String directoryName) {
    FileFolderManager.createFolder(getAbsolutePath(componentId));
  }

  public static void createTempPath(String sComponentId, String sDirectoryName) {
    FileFolderManager.createFolder(getAbsolutePath(sComponentId));
  }

  public static void createGlobalTempPath(String sDirectoryName) {
    FileFolderManager.createFolder(getTemporaryPath() + sDirectoryName);
  }

  public static void createAbsoluteIndexPath(String particularSpace, String componentId) {
    FileFolderManager.createFolder(IndexFileManager.getAbsoluteIndexPath(particularSpace,
        componentId));
  }

  public static void deleteAbsolutePath(String sSpaceId, String sComponentId, String sDirectoryName) {
    FileFolderManager.deleteFolder(getAbsolutePath(sComponentId) + sDirectoryName);
  }

  public static void deleteTempPath(String sSpaceId, String sComponentId, String sDirectoryName) {
    FileFolderManager.deleteFolder(getAbsolutePath(sComponentId));
  }

  public static void deleteAbsoluteIndexPath(String particularSpace, String sComponentId) {
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

  public static String getFileIcon(boolean small, String filename, boolean isReadOnly) {
    String path = URLManager.getApplicationURL() + uploadSettings.getString("FileIconsPath");
    String extension = FilenameUtils.getExtension(filename);
    if (!StringUtil.isDefined(extension)) {
      extension = filename;
    }
    String fileIcon = uploadSettings.getString(extension.toLowerCase(Locale.getDefault()));
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
    return UnitUtil.formatMemSize(lSize);
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
   * @return
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

  private FileRepositoryManager() {
  }
}
