package com.stratelia.webactiv.util;

import java.util.Hashtable;
import java.util.Map;

/**
 * 
 * @author NEY
 * @version
 */

public class FileServerUtils extends Object {

  /**
   * Replace chars that have special meanings in url by their http substitute.
   * 
   * @param toParse
   *          the string which chars that have special meanings in url by their
   *          http substitute.
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
   * 
   * @param toParse
   *          the string which accented chars are replaced by non-accented
   *          chars.
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
    return newLogicalName;
  }

  /**
   * Return the full url to access an attachment from web site
   * 
   * @param fullContext
   *          something like serverName:serverPort/context
   * 
   */
  public static String getWebUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType,
      String subDirectory) {
    StringBuffer url = new StringBuffer();

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(newLogicalName).append("?ComponentId=").append(componentId)
        .append("&SourceFile=").append(physicalName).append("&MimeType=")
        .append(mimeType).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getUrl(String componentId, String logicalName) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId);
    return url.toString();
  }

  public static String getUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType,
      String subDirectory) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&SourceFile=").append(
        physicalName).append("&MimeType=").append(mimeType).append(
        "&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getOnlineURL(String componentId, String logicalName,
      String physicalName, String mimeType, String subDirectory) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/OnlineFileServer/").append(newLogicalName)
        .append("?ComponentId=").append(componentId).append("&SourceFile=")
        .append(physicalName).append("&MimeType=").append(mimeType).append(
            "&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getAliasURL(String componentId, String logicalName,
      String attachmentId) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/AliasFileServer/").append(newLogicalName)
        .append("?ComponentId=").append(componentId).append("&AttachmentId=")
        .append(attachmentId);
    return url.toString();
  }

  public static String getAliasURL(String componentId, String logicalName,
      String documentId, String versionId) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/AliasFileServer/").append(newLogicalName)
        .append("?ComponentId=").append(componentId).append("&DocumentId=")
        .append(documentId).append("&VersionId=" + versionId);
    return url.toString();
  }

  public static Map<String, String> getMappedUrl(String spaceId,
      String componentId, String logicalName, String physicalName,
      String mimeType, String subDirectory) {
    Map<String, String> parameters = new Hashtable<String, String>();
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
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(m_context).append("/FileServer/").append(newLogicalName).append(
        "?SourceFile=").append(physicalName).append(
        "&TypeUpload=link&MimeType=").append(mimeType);
    return url.toString();
  }

  public static String getUrl(String spaceId, String componentId,
      String userId, String logicalName, String physicalName, String mimeType,
      boolean archiveIt, int pubId, int nodeId, String subDirectory) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    char archiveItStr = 'N';
    if (archiveIt) {
      archiveItStr = 'Y';
    }
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(m_context).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&UserId=").append(userId)
        .append("&SourceFile=").append(physicalName).append("&MimeType=")
        .append(mimeType);
    url.append("&ArchiveIt=").append(archiveItStr).append("&PubId=").append(
        pubId).append("&NodeId=").append(nodeId).append("&Directory=").append(
        subDirectory);
    return url.toString();
  }

  public static String getUrlToTempDir(String logicalName, String physicalName,
      String mimeType) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String newLogicalName = replaceSpecialChars(logicalName);
    url.append(m_context).append("/TempFileServer/").append(newLogicalName);
    return url.toString();
  }
}