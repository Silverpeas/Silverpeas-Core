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

import org.silverpeas.core.util.URLEncoder;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NEY
 * @version
 */
public class FileServerUtils {

  public static final String COMPONENT_ID_PARAMETER = "ComponentId";
  public static final String SOURCE_FILE_PARAMETER = "SourceFile";
  public static final String DIRECTORY_PARAMETER = "Directory";
  public static final String ARCHIVE_IT_PARAMETER = "ArchiveIt";
  public static final String DIR_TYPE_PARAMETER = "DirType";
  public static final String USER_ID_PARAMETER = "UserId";
  public static final String MIME_TYPE_PARAMETER = "MimeType";
  public static final String TYPE_UPLOAD_PARAMETER = "TypeUpload";
  public static final String NODE_ID_PARAMETER = "NodeId";
  public static final String PUBLICATION_ID_PARAMETER = "PubId";
  public static final String SIZE_PARAMETER = "Size";

  private static final SettingBundle lookSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");

  /**
   * Replace chars that have special meanings in url by their http substitute.
   *
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
   *
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
   *
   *
   * @param componentId
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @param subDirectory
   * @return
   */
  public static String getWebUrl(String componentId, String logicalName, String physicalName,
      String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathParamValue(logicalName);
    url.append(newLogicalName).append("?ComponentId=").append(componentId).
        append("&SourceFile=").append(physicalName).append("&MimeType=").append(
        mimeType).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getUrl(String componentId, String logicalName) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId);
    return url.toString();
  }

  public static String getUrl(String componentId, String logicalName, String physicalName,
      String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&SourceFile=").append(physicalName).append(
        "&MimeType=").append(mimeType).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getOnlineURL(String componentId, String logicalName,
      String physicalName, String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/OnlineFileServer/").append(newLogicalName).
        append("?ComponentId=").append(componentId).append("&SourceFile=").append(physicalName).
        append("&MimeType=").append(mimeType).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getAttachmentURL(String componentId, String logicalName, String attachmentId,
      String lang) {
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    StringBuilder url = new StringBuilder();
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }
    url.append("/attached_file/").append("componentId/").append(URLEncoder.encodePathSegment(
        componentId)).append("/attachmentId/").append(URLEncoder.encodePathSegment(attachmentId)).
        append("/lang/").append(URLEncoder.encodePathSegment(language)).append("/name/").
        append(newLogicalName);
    return url.toString();
  }

  /**
   * Gets the URL of the specified image with the specified size.
   * <p/>
   * Each image uploaded in Silverpeas are kept with their original size. From them, a set of
   * resized images are computed. This method is to get the URL of the resized version of an
   * uploaded image.
   * @param originalImageURL the URL of the original, non-resized, image.
   * @param sizeParams the size of the image to get. The size can be specified either a key in the
   * {@code org.silverpeas.lookAndFeel.generalLook} bundle or as a dimension. The keys of the
   * properties indicating an image size are always prefixed by the 'image.size' term. The
   * dimension of an image must be in the form of WIDTHxHEIGHT with WIDTH the width in pixels of
   * the image and HEIGHT the height in pixels of the image. WIDTH or HEIGHT can be omitted but the
   * 'x' character is required. If null, empty or or not well formed, the original image URL is
   * then returned.
   * @return the URL of the image with the specified size.
   */
  public static String getImageURL(String originalImageURL, String sizeParams) {
    String resizedImagePath = originalImageURL;
    String size = sizeParams;
    if (StringUtil.isDefined(originalImageURL) && StringUtil.isDefined(sizeParams)) {
      if (sizeParams.startsWith("image.size.")) {
        size = lookSettings.getString(sizeParams);
      }
      if (StringUtil.isDefined(size) && size.length() > 1 && size.contains("x")) {
        // image handled by the old FileServer service
        if (originalImageURL.contains("/FileServer/") ||
            originalImageURL.contains("/GalleryInWysiwyg/")) {
          resizedImagePath = originalImageURL + "&Size=" + size;
        } else {
          int lastSepIndex = originalImageURL.lastIndexOf("/");
          if (originalImageURL.contains("/attached_file/")) {
            // asks for an image attached to a contribution (a form, a WYSIWYG, ...)
            size = "size/" + size;
            lastSepIndex = originalImageURL.substring(0, lastSepIndex).lastIndexOf("/");

          }
          resizedImagePath = (lastSepIndex == -1 ? size + "/" + originalImageURL :
              originalImageURL.substring(0, lastSepIndex + 1) + size +
                  originalImageURL.substring(lastSepIndex));
        }
      }
    }
    return resizedImagePath;
  }

  public static String getAliasURL(String componentId, String logicalName, String attachmentId) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/AliasFileServer/").append(newLogicalName).
        append("?ComponentId=").append(componentId).append("&AttachmentId=").
        append(attachmentId);
    return url.toString();
  }

  public static Map<String, String> getMappedUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType, String subDirectory) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("SpaceId", spaceId);
    parameters.put("ComponentId", componentId);
    parameters.put("SourceFile", physicalName);
    parameters.put("MimeType", mimeType);
    parameters.put("Directory", subDirectory);
    return parameters;
  }

  public static String getUrl(String componentId, String name, String mimeType, String subDirectory) {
    String url = getUrl(componentId, name, name, mimeType, subDirectory);
    return url;
  }

  public static String getUrl(String logicalName, String physicalName, String mimeType) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?SourceFile=").append(physicalName).append("&TypeUpload=link&MimeType=").append(mimeType);
    return url.toString();
  }

  public static String getUrl(String componentId, String userId, String logicalName,
      String physicalName, String mimeType, boolean archiveIt, int pubId, int nodeId,
      String subDirectory) {
    StringBuilder url = new StringBuilder();
    char archiveItStr = 'N';
    if (archiveIt) {
      archiveItStr = 'Y';
    }
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/FileServer/").append(newLogicalName).append(
        "?ComponentId=").append(componentId).append("&UserId=").append(userId).
        append("&SourceFile=").append(URLEncoder.encodePathParamValue(physicalName)).append(
        "&MimeType=").append(mimeType).append("&ArchiveIt=").append(archiveItStr).append("&PubId=").
        append(pubId).append("&NodeId=").append(nodeId).append("&Directory=").append(subDirectory);
    return url.toString();
  }

  public static String getUrlToTempDir(String logicalName) {
    StringBuilder path = new StringBuilder();
    path.append(getApplicationContext()).append("/TempFileServer/").append(URLEncoder.
        encodePathSegment(logicalName));
    try {
      URI uri = new URI(null, null, path.toString(), null);
      return uri.toString();
    } catch (URISyntaxException ex) {
      path = new StringBuilder();
      String newLogicalName = URLEncoder.encodePathSegment(logicalName);
      path.append(getApplicationContext()).append("/TempFileServer/").append(newLogicalName);
      return path.toString();
    }

  }

  /**
   * Replace chars from filename String which can't be used in a file name with '_'.
   *
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
    String applicationContext = URLUtil.getApplicationURL();
    if (applicationContext.endsWith("/")) {
      applicationContext = applicationContext.substring(0, applicationContext.length() - 1);
    }
    return applicationContext;
  }
}
