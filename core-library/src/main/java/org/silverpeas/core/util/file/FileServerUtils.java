/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import org.silverpeas.core.util.URLEncoder;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author NEY
 */
public class FileServerUtils {

  public static final String COMPONENT_ID_PARAMETER = "ComponentId";
  public static final String SOURCE_FILE_PARAMETER = "SourceFile";
  public static final String DIRECTORY_PARAMETER = "Directory";
  public static final String ARCHIVE_IT_PARAMETER = "ArchiveIt";
  public static final String DIR_TYPE_PARAMETER = "DirType";
  public static final String USER_ID_PARAMETER = "UserId";
  public static final String MIME_TYPE_PARAMETER = "MimeType";
  public static final String NODE_ID_PARAMETER = "NodeId";
  public static final String PUBLICATION_ID_PARAMETER = "PubId";
  public static final String SIZE_PARAMETER = "Size";

  private static final SettingBundle lookSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  private static final String FILE_SERVER = "/FileServer/";
  private static final String COMPONENT_ID = "?ComponentId=";
  private static final String SOURCE_FILE = "&SourceFile=";
  private static final String MIME_TYPE = "&MimeType=";
  private static final String DIRECTORY = "&Directory=";
  private static final String URL_PATH_SEP = "/";

  protected FileServerUtils() {
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

  public static String getUrl(String componentId, String logicalName) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append(FILE_SERVER).append(newLogicalName).append(
        COMPONENT_ID).append(componentId);
    return url.toString();
  }

  public static String getUrl(String componentId, String logicalName, String physicalName,
      String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append(FILE_SERVER).append(newLogicalName).append(
        COMPONENT_ID).append(componentId).append(SOURCE_FILE).append(physicalName).append(
        MIME_TYPE).append(mimeType).append(DIRECTORY).append(subDirectory);
    return url.toString();
  }

  public static String getOnlineURL(String componentId, String logicalName,
      String physicalName, String mimeType, String subDirectory) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/OnlineFileServer/").append(newLogicalName).
        append(COMPONENT_ID).append(componentId).append(SOURCE_FILE).append(physicalName).
        append(MIME_TYPE).append(mimeType).append(DIRECTORY).append(subDirectory);
    return url.toString();
  }

  public static String getAttachmentURL(String componentId, String logicalName, String attachmentId,
      String lang) {
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    StringBuilder url = new StringBuilder();
    String language = lang;
    if (language == null) {
      language = I18NHelper.DEFAULT_LANGUAGE;
    }
    url.append("/attached_file/").append("componentId/").append(URLEncoder.encodePathSegment(
            componentId)).append("/attachmentId/").append(URLEncoder.encodePathSegment(attachmentId)).
        append("/lang/").append(URLEncoder.encodePathSegment(language)).append("/name/").
        append(newLogicalName);
    return url.toString();
  }

  /**
   * Gets the URL of the specified image with the specified size.
   * <p>
   * Each image uploaded in Silverpeas are kept with their original size. From them, a set of
   * resized images are computed. This method is to get the URL of the resized version of an
   * uploaded image.
   *
   * @param originalImageURL the URL of the original, non-resized, image.
   * @param sizeParams the size of the image to get. The size can be specified either a key in the
   * {@code org.silverpeas.lookAndFeel.generalLook} bundle or as a dimension. The keys of the
   * properties indicating an image size are always prefixed by the 'image.size' term. The dimension
   * of an image must be in the form of <code>WIDTHxHEIGHT</code> with WIDTH the width in pixels of
   * the image and HEIGHT the height in pixels of the image. WIDTH or HEIGHT can be omitted but the
   * 'x' character is required. If null, empty or not well-formed, the original image URL is then
   * returned.
   * @return the URL of the image with the specified size.
   */
  public static String getImageURL(String originalImageURL, String sizeParams) {
    if (StringUtil.isNotDefined(originalImageURL) || StringUtil.isNotDefined(sizeParams)) {
      return originalImageURL;
    }

    String resizedImagePath = originalImageURL;
    String size = sizeParams.startsWith("image.size.") ? lookSettings.getString(sizeParams) :
        sizeParams;
    if (StringUtil.isDefined(size) && size.length() > 1 && size.contains("x")) {
      // image handled by the old FileServer service
      if (originalImageURL.contains(FILE_SERVER) ||
          originalImageURL.contains("/GalleryInWysiwyg/")) {
        resizedImagePath = originalImageURL + "&Size=" + size;
      } else {
        int lastSepIndex = originalImageURL.lastIndexOf(URL_PATH_SEP);
        if (originalImageURL.contains("/attached_file/")) {
          // asks for an image attached to a contribution (a form, a WYSIWYG, ...)
          size = "size/" + size;
          lastSepIndex = originalImageURL.substring(0, lastSepIndex).lastIndexOf(URL_PATH_SEP);

        }
        resizedImagePath = (lastSepIndex == -1 ? size + URL_PATH_SEP + originalImageURL :
            originalImageURL.substring(0, lastSepIndex + 1) + size +
                originalImageURL.substring(lastSepIndex));
      }
    }
    return resizedImagePath;
  }

  public static String getUrl(String componentId, String name, String mimeType,
      String subDirectory) {
    return getUrl(componentId, name, name, mimeType, subDirectory);
  }

  public static String getUrl(String logicalName, String physicalName, String componentId) {
    StringBuilder url = new StringBuilder();
    String newLogicalName = URLEncoder.encodePathSegment(logicalName);
    url.append(getApplicationContext()).append("/SilverCrawlerFileServer/").append(newLogicalName).append(
        "?SourceFile=").append(physicalName).append("&TypeUpload=link&ComponentId=").append(componentId);
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
    if (applicationContext.endsWith(FileServerUtils.URL_PATH_SEP)) {
      applicationContext = applicationContext.substring(0, applicationContext.length() - 1);
    }
    return applicationContext;
  }
}
