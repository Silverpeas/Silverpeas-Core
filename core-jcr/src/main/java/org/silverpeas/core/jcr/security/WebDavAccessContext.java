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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import java.net.URLDecoder;

/**
 * A user accesses the JCR through WebDAV. In Silverpeas, the WebDAV protocol is used to edit
 * directly a document in Silverpeas; so at each WebDav access corresponds the path of the document
 * the user is granted to access.
 * @author mmoquillon
 */
public class WebDavAccessContext implements AccessContext {

  public static final String AUTHORIZED_DOCUMENT_PATH_ATTRIBUTE = "AuthorizedDocumentPath";

  private final String grantedDocumentPath;

  public WebDavAccessContext(final String grantedDocumentPath) {
    this.grantedDocumentPath = grantedDocumentPath;
  }

  @Override
  public boolean isGranted(final String jcrPath, final long permissions) {
    if (StringUtil.isNotDefined(grantedDocumentPath)) {
      return false;
    }
    String grantedJcrPath = URLDecoder.decode(grantedDocumentPath, Charsets.UTF_8);
    return grantedJcrPath.length() > jcrPath.length() ? grantedJcrPath.contains(jcrPath) :
        jcrPath.contains(grantedJcrPath);
  }
}
