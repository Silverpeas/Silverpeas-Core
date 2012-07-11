/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jcrutil.security.impl;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silverpeas.util.StringUtil;

/**
 * @author ehugonnet
 */
public class SilverpeasDigestCredentialsProvider implements CredentialsProvider {

  private static final Logger log = LoggerFactory.getLogger(
      SilverpeasDigestCredentialsProvider.class);

  @Override
  public Credentials getCredentials(HttpServletRequest request) throws LoginException,
      ServletException {
    String authorization = request.getHeader(DavConstants.HEADER_AUTHORIZATION);
    if (!StringUtil.isDefined(authorization)) {
      throw new LoginException();
    }
    if (log.isDebugEnabled()) {
      log.debug("authorization: " + authorization);
    }
    return createDigestCredentials(request, authorization);
  }

  protected DigestCredentials createDigestCredentials(HttpServletRequest request,
      String authorization) {
    if (authorization == null) {
      return (null);
    }
    if (!authorization.startsWith("Digest ")) {
      return (null);
    }
    authorization = authorization.substring(7).trim();
    String[] tokens = authorization.split(",(?=(?:[^\"]*\"[^\"]*\")+$)");
    String userName = null;
    String realmName = null;
    String nOnce = null;
    String nc = null;
    String cnonce = null;
    String qop = null;
    String uri = null;
    String response = null;
    String method = request.getMethod();

    for (String currentToken : tokens) {
      if (currentToken.length() == 0) {
        continue;
      }

      int equalSign = currentToken.indexOf('=');
      if (equalSign < 0) {
        return null;
      }
      String currentTokenName = currentToken.substring(0, equalSign).trim();
      String currentTokenValue = currentToken.substring(equalSign + 1).trim();
      if ("username".equals(currentTokenName)) {
        userName = removeQuotes(currentTokenValue);
      }
      if ("realm".equals(currentTokenName)) {
        realmName = removeQuotes(currentTokenValue, true);
      }
      if ("nonce".equals(currentTokenName)) {
        nOnce = removeQuotes(currentTokenValue);
      }
      if ("nc".equals(currentTokenName)) {
        nc = removeQuotes(currentTokenValue);
      }
      if ("cnonce".equals(currentTokenName)) {
        cnonce = removeQuotes(currentTokenValue);
      }
      if ("qop".equals(currentTokenName)) {
        qop = removeQuotes(currentTokenValue);
      }
      if ("uri".equals(currentTokenName)) {
        uri = removeQuotes(currentTokenValue);
      }
      if ("response".equals(currentTokenName)) {
        response = removeQuotes(currentTokenValue);
      }
    }
    if ((userName == null) || (realmName == null) || (nOnce == null) ||
        (uri == null) || (response == null)) {
      return null;
    }
    String a2 = method + ":" + uri;
    String md5a2 = Text.md5(a2);

    DigestCredentials dc = new DigestCredentials();
    dc.setUsername(userName);
    dc.setClientDigest(response);
    dc.setNonce(nOnce);
    dc.setNc(nc);
    dc.setCnonce(cnonce);
    dc.setQop(qop);
    dc.setRealm(realmName);
    dc.setMd5a2(md5a2);
    return dc;

  }

  /**
   * Removes the quotes on a string. RFC2617 states quotes are optional for all parameters except
   * realm.
   */
  protected static String removeQuotes(String quotedString, boolean quotesRequired) {
    if (quotedString.length() > 0 && quotedString.charAt(0) != '"' &&
        !quotesRequired) {
      return quotedString;
    }
    if (quotedString.length() > 2) {
      return quotedString.substring(1, quotedString.length() - 1);
    }
    return "";
  }

  /**
   * Removes the quotes on a string.
   */
  protected static String removeQuotes(String quotedString) {
    return removeQuotes(quotedString, false);
  }
}
