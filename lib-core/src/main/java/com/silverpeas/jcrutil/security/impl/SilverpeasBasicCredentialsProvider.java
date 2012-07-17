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

import com.silverpeas.util.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.util.Base64;
import org.apache.jackrabbit.webdav.DavConstants;

/**
 * This code is a derivative of the org.apache.jackrabbit.server.BasicCredentialsProvider form
 * Apache Jackrabbit whic is under Apache 2.0 License.
 * @author ehugonnet
 */
public class SilverpeasBasicCredentialsProvider implements CredentialsProvider {

  private final String defaultHeaderValue;

  /**
   * Constructs a new BasicCredentialsProvider with the given default value {
   * @see #getCredentials for details.
   * @param defaultHeaderValue
   */
  public SilverpeasBasicCredentialsProvider(String defaultHeaderValue) {
    this.defaultHeaderValue = defaultHeaderValue;
  }

  /**
   * {@inheritDoc} Build a {@link Credentials} object for the given authorization header. The creds
   * may be used to login to the repository. If the specified header string is <code>null</code> the
   * behaviour depends on the {@link #defaultHeaderValue} field:<br>
   * <ul>
   * <li>if this field is <code>null</code>, a LoginException is thrown. This is suiteable for
   * clients (eg. webdav clients) for with sending a proper authorization header is not possible, if
   * the server never send a 401.
   * <li>if this an empty string, null-credentials are returned, thus forcing an null login on the
   * repository
   * <li>if this field has a 'user:password' value, the respective simple credentials are generated.
   * </ul>
   * <p/>
   * If the request header is present but cannot be parsed a <code>ServletException</code> is
   * thrown.
   * @param request the servlet request
   * @return credentials or <code>null</code>.
   * @throws ServletException If the Authorization header cannot be decoded.
   * @throws LoginException if no suitable auth header and missing-auth-mapping is not present
   */
  @Override
  public Credentials getCredentials(HttpServletRequest request) throws LoginException,
      ServletException {
    try {
      String authHeader = request.getHeader(DavConstants.HEADER_AUTHORIZATION);
      if (authHeader != null) {
        String[] authStr = authHeader.split(" ");
        if (authStr.length >= 2 && authStr[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          Base64.decode(authStr[1].toCharArray(), out);
          String charset = "ISO-8859-1";
          if (StringUtil.isDefined(request.getCharacterEncoding())) {
            charset = request.getCharacterEncoding();
          }
          String decAuthStr = out.toString(charset);
          int pos = decAuthStr.indexOf(':');
          String userid = decAuthStr.substring(0, pos);
          String passwd = decAuthStr.substring(pos + 1);
          return new SimpleCredentials(userid, passwd.toCharArray());
        }
        throw new ServletException("Unable to decode authorization.");
      } else {
        // check special handling
        if (defaultHeaderValue == null) {
          throw new LoginException();
        } else if (defaultHeaderValue.equals("")) {
          return null;
        } else {
          int pos = defaultHeaderValue.indexOf(':');
          if (pos < 0) {
            return new SimpleCredentials(defaultHeaderValue, new char[0]);
          } else {
            return new SimpleCredentials(defaultHeaderValue.substring(0, pos), defaultHeaderValue.
                substring(pos + 1).toCharArray());
          }
        }
      }
    } catch (IOException e) {
      throw new ServletException("Unable to decode authorization: " + e.toString());
    }
  }
}
