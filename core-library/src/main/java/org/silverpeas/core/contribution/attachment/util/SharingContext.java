/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.util;

import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.net.URI;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharingContext {

  private static Pattern REGEXPR_SHARED_ATTACHMENT =
      Pattern.compile("(?i)src=\"(.+/attachmentId/[^\"]+)");

  private String baseURI;
  private String token;

  public SharingContext(String baseURI, String token) {
    this.baseURI = baseURI;
    this.token = token;
  }

  public String getBaseURI() {
    return baseURI;
  }
  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }
  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * This method reads a text content in order to identify all attachment URIs and to transform
   * them into shared attachment URIs.
   * @param text the text content to modify
   * @return the text containing all attachment URI conversions.
   */
  public String applyOn(String text) {
    Matcher matcher;
    String newStr = text;
    while ((matcher = REGEXPR_SHARED_ATTACHMENT.matcher(newStr)).find()) {
      String currentURL = matcher.group(1);
      newStr = matcher.replaceFirst("src=\"" + convertURLToSharedOne(currentURL));
    }
    return newStr;
  }

  /**
   * URL looks like :
   * http://localhost:8000/silverpeas/attached_file/componentId/kmelia144/attachmentId/7088b9d6
   * -ec5a-4a9c-8c0e-dcb77eed704e/lang/fr/name/Penguins.jpg
   * must be converted into :
   * http://localhost:8000
   * /silverpeas/services/attachments/kmelia144/ca36bf15-8e52-4d53-8692-0090845ac409
   * /7088b9d6-ec5a-4a9c-8c0e-dcb77eed704e/Penguins.jpg
   * @return
   */
  private String convertURLToSharedOne(String url) {
    String[] parts = StringUtil.split(url, "/");
    String name = parts[ArrayUtil.indexOf(parts, "name") + 1];
    String id = parts[ArrayUtil.indexOf(parts, "attachmentId") + 1];
    String instanceId = parts[ArrayUtil.indexOf(parts, "componentId") + 1];
    return getBaseURI() + "sharing/attachments/" + instanceId + "/" + getToken() +
        "/" + id + "/" + name;
  }

  /**
   * Gets the shared URI of the given attachments according to the sharing context.
   * @param attachment the attachment for which the shared URI will be computed.
   * @return the shared URI of the given attachment.
   */
  public URI getSharedUriOf(SimpleDocument attachment) {
    URI sharedUri;
    try {
      sharedUri =
          URI.create(getBaseURI() + "sharing/attachments/" + attachment.getInstanceId() + "/" +
              getToken() + "/" + attachment.getId() + "/" +
              URLEncoder.encode(attachment.getFilename(), CharEncoding.UTF_8));
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
    return sharedUri;
  }
}
