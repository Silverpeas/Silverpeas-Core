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

import org.silverpeas.core.util.URLUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.memory.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import static java.io.File.separatorChar;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Norbert CHAIX
 * @version
 */
public class FileRepositoryManager {

  static String tempPath = "";
  static String domainPropertiesFolderPath;
  static String domainAuthenticationPropertiesFolderPath;
  final static SettingBundle uploadSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.uploads.uploadSettings");
  public static final String CONTEXT_TOKEN = ",";

  static {
    tempPath = ResourceLocator.getGeneralSettingBundle().getString("tempPath");
    if (!tempPath.endsWith(File.separator)) {
      tempPath = tempPath + File.separatorChar;
    }

    File path = FileUtils
        .getFile(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas");
    domainPropertiesFolderPath =
        FileUtils.getFile(path, "domains").getAbsolutePath() + separatorChar;
    domainAuthenticationPropertiesFolderPath =
        FileUtils.getFile(path, "authentication").getAbsolutePath() + separatorChar;
  }

  /**
   * Gets the path of the directory in which all the resources related to the security in Silverpeas
   * are stored.
   * @return the path of the Silverpeas security directory.
   */
  public static String getSecurityDirPath() {
    return ResourceLocator.getGeneralSettingBundle().getString("securityPath") + File.separator;
  }

  /**
   * @deprecated
   * @param sSpaceId
   * @param sComponentId
   * @return
   * @deprecated
   */
  @Deprecated
  public static String getAbsolutePath(String sSpaceId, String sComponentId) {
    return getUploadPath() + sComponentId + separatorChar;
  }

  public static String getAbsolutePath(String sComponentId) {
    return getUploadPath() + sComponentId + separatorChar;
  }

  public static String getAvatarPath() {
    return ResourceLocator.getGeneralSettingBundle().getString(
        "avatar.path", getUploadPath() + "avatar");
  }

  /**
   * Gets the path of the repository into which attachments and other files are uploaded in
   * Silverpeas.
   *
   * @return the path of the root repository for uploads.
   */
  public static String getUploadPath() {
    return ResourceLocator.getGeneralSettingBundle().getString(
        "uploadsPath") + separatorChar;
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

  public static void createGlobalTempPath(String sDirectoryName) {
    FileFolderManager.createFolder(getTemporaryPath() + sDirectoryName);
  }

  public static void deleteAbsolutePath(String sSpaceId, String sComponentId, String sDirectoryName) {
    FileFolderManager.deleteFolder(getAbsolutePath(sComponentId) + sDirectoryName);
  }

  public static String getFileIcon(String extension) {
    return getFileIcon(false, extension);
  }

  public static String getFileIcon(boolean small, String filename) {
    String path = URLUtil.getApplicationURL() + uploadSettings.getString("FileIconsPath");
    String extension = defaultStringIfNotDefined(FilenameUtils.getExtension(filename), filename);
    extension = defaultStringIfNotDefined(extension);
    String fileIcon;
    try {
      fileIcon = uploadSettings.getString(extension.toLowerCase(Locale.getDefault()));
    } catch (MissingResourceException mre) {
      fileIcon = uploadSettings.getString("unknown");
    }
    if (small && fileIcon != null) {
      fileIcon = fileIcon.substring(0, fileIcon.lastIndexOf(".gif")) + "Small.gif";
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
   * Gets the file size limit for an upload.
   * @return
   */
  public static long getUploadMaximumFileSize() {
    return uploadSettings.getLong("MaximumFileSize",
        UnitUtil.convertTo(10, MemoryUnit.MB, MemoryUnit.B));
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
    String path = ResourceLocator.getGeneralSettingBundle().getString(
        "exportTemplatePath");
    if (!path.endsWith("/")) {
      path += "/";
    }
    return path;
  }

  private FileRepositoryManager() {
  }
}
