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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.util;

import java.net.URISyntaxException;
import java.util.Map;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.net.URI;
import java.util.HashMap;

/**
 * @author NEY
 * @version
 */
public class FileServerUtils extends Object {

  /**
   * Replace chars that have special meanings in url by their http substitute.
   * @param toParse the string which chars that have special meanings in url by their http
   * substitute.
   * @return a string without url meaning chars.
   */
  public static String replaceSpecialChars(String toParse) {
    String newLogicalName = toParse.replace("#", "%23");
    newLogicalName = newLogicalName.replace("%", "%25");
    newLogicalName = newLogicalName.replace("&", "%26");
    newLogicalName = newLogicalName.replace(";", "%3B");
    return newLogicalName;
  }

  /**
   * Replace accented chars from a string.
   * @param toParse the string which accented chars are replaced by non-accented chars.
   * @return a string with all its accented chars replaced.
   */
  public static String replaceAccentChars(String toParse) {

    String newLogicalName = toParse.replace('é', 'e');
    newLogicalName = newLogicalName.replace('è', 'e');
    newLogicalName = newLogicalName.replace('ë', 'e');
    newLogicalName = newLogicalName.replace('ê', 'e');
    newLogicalName = newLogicalName.replace('ö', 'o');
    newLogicalName = newLogicalName.replace('ô', 'o');
    newLogicalName = newLogicalName.replace('õ', 'o');
    newLogicalName = newLogicalName.replace('ò', 'o');
    newLogicalName = newLogicalName.replace('ï', 'i');
    newLogicalName = newLogicalName.replace('î', 'i');
    newLogicalName = newLogicalName.replace('ì', 'i');
    newLogicalName = newLogicalName.replace('ñ', 'n');
    newLogicalName = newLogicalName.replace('ü', 'u');
    newLogicalName = newLogicalName.replace('û', 'u');
    newLogicalName = newLogicalName.replace('ù', 'u');
    newLogicalName = newLogicalName.replace('ç', 'c');
    newLogicalName = newLogicalName.replace('à', 'a');
    newLogicalName = newLogicalName.replace('ä', 'a');
    newLogicalName = newLogicalName.replace('ã', 'a');
    newLogicalName = newLogicalName.replace('â', 'a');
    newLogicalName = newLogicalName.replace('°', '_');
    return newLogicalName;
  }

  /**
   * Return the full url to access an attachment from web site
   * @param fullContext something like serverName:serverPort/context
   */
  public static String getWebUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType,
      String subDirectory) {
    StringBuilder url = new StringBuilder();

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(newLogicalName).append("?ComponentId=").append(componentId).
        append("&SourceFile=").append(physicalName).append("&MimeType=").append(
        mimeType).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getUrl(String componentId, String logicalName) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId);
    return url.toString();
  }

  public static String getUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType,
      String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&SourceFile=").append(
        physicalName).append("&MimeType=").append(mimeType).append(
        "&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getOnlineURL(String componentId, String logicalName,
      String physicalName, String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/OnlineFileServer/").append(newLogicalName).
        append("?ComponentId=").append(componentId).append("&SourceFile=").
        append(physicalName).append("&MimeType=").append(mimeType).append(
        "&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getAttachmentURL(String componentId, String logicalName,
      String attachmentId, String lang) {
    SilverTrace.debug("util", "FileServerUtils.getRestAttachmentURL",
        "root.MSG_GEN_ENTER_METHOD",
        "componentId = " + componentId + ", logicalName = " + logicalName + ", "
        + "attachmentId = " + attachmentId + ", lang = " + lang);
    StringBuilder url = new StringBuilder();
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }
    SilverTrace.debug("util", "FileServerUtils.getRestAttachmentURL",
        "root.MSG_GEN_PARAM_VALUE", "language = " + language);
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append("/attached_file/").append("componentId/").append(componentId).append(
        "/attachmentId/").
        append(attachmentId).append("/lang/").append(language).append("/name/").
        append(newLogicalName);
    return url.toString();
  }

  public static String getVersionedDocumentURL(String componentId,
      String logicalName, String documentId, String versionId) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append("/attached_file").append("/componentId/").
        append(componentId).append("/documentId/").append(documentId).append(
        "/versionId/").append(versionId).append("/name/").append(newLogicalName);
    return url.toString();
  }

  public static String getAliasURL(String componentId, String logicalName,
      String attachmentId) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/AliasFileServer/").append(newLogicalName).
        append("?ComponentId=").append(componentId).append("&AttachmentId=").
        append(attachmentId);
    return url.toString();
  }

  public static String getAliasURL(String componentId, String logicalName,
      String documentId, String versionId) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/AliasFileServer/").append(newLogicalName).
        append("?ComponentId=").append(componentId).append("&DocumentId=").
        append(documentId).append("&VersionId=").append(versionId);
    return url.toString();
  }

  public static Map<String, String> getMappedUrl(String spaceId,
      String componentId, String logicalName, String physicalName,
      String mimeType, String subDirectory) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("SpaceId", spaceId);
    parameters.put("ComponentId", componentId);
    parameters.put("SourceFile", physicalName);
    parameters.put("MimeType", mimeType);
    parameters.put("Directory", subDirectory);
    return parameters;
  }

  public static String getUrl(String spaceId, String componentId, String name,
      String mimeType, String subDirectory) {
    String url = getUrl(spaceId, componentId, name, name, mimeType,
        subDirectory);
    return url;
  }

  public static String getUrl(String logicalName, String physicalName,
      String mimeType) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?SourceFile=").append(physicalName).append(
        "&TypeUpload=link&MimeType=").append(mimeType);
    return url.toString();
  }

  public static String getUrl(String spaceId, String componentId,
      String userId, String logicalName, String physicalName, String mimeType,
      boolean archiveIt, int pubId, int nodeId, String subDirectory) {
    StringBuilder url = new StringBuilder();
    char archiveItStr = 'N';
    if (archiveIt) {
      archiveItStr = 'Y';
    }
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&UserId=").append(userId).
        append("&SourceFile=").append(physicalName).append("&MimeType=").append(
        mimeType);
    url.append("&ArchiveIt=").append(archiveItStr).append("&PubId=").append(
        pubId).append("&NodeId=").append(nodeId).append("&Directory=").append(
        subDirectory);
    return url.toString();
  }

  public static String getUrlToTempDir(String logicalName) {
    StringBuilder path = new StringBuilder();
    path.append(getApplicationContext()).append("/TempFileServer/").append(logicalName);
    try {
      URI uri = new URI(null, null, path.toString(), null);
      return uri.toString();
    } catch (URISyntaxException ex) {
      path = new StringBuilder();
      String newLogicalName = replaceSpecialChars(logicalName);
      path.append(getApplicationContext()).append("/TempFileServer/").append(newLogicalName);
      return path.toString();
    }

  }

  /**
   * Replace chars from filename String which can't be used in a file name with '_'.
   * @param toParse the name of the file.
   * @return the name of the file with incorrect chars replaced by '_'.
   */
  public static String replaceInvalidPathChars(String toParse) {
    String newLogicalName = toParse.replace('\'', '_');
    newLogicalName = newLogicalName.replace('/', '_');
    newLogicalName = newLogicalName.replace(':', '_');
    newLogicalName = newLogicalName.replace('*', '_');
    newLogicalName = newLogicalName.replace('?', '_');
    newLogicalName = newLogicalName.replace('"', '_');
    newLogicalName = newLogicalName.replace('<', '_');
    newLogicalName = newLogicalName.replace('>', '_');
    newLogicalName = newLogicalName.replace('|', '_');
    return newLogicalName;
  }

  public static String getApplicationContext() {
    String applicationContext = GeneralPropertiesManager.getGeneralResourceLocator().
        getString("ApplicationURL");
    if (applicationContext.endsWith("/")) {
      applicationContext = applicationContext.substring(0, applicationContext.length() - 1);
    }
    return applicationContext;
  }
}
