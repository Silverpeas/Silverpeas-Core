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

package com.silverpeas.lookV5;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;

/**
 * <pre>
 * This servlet allows user to display RSS workflow inside a web page. 
 * The call must be done in Ajax using JQuery framework. 
 * You must use two following ajax parameters:
 * </pre>
 * <ul>
 * <li><b>encoding</b>UTF-8 (default value) or ISO-8859-1</li>
 * <li><b>loadedUrl</b> the RSS URL to load in Ajax</li>
 * </ul>
 * See below a JQuery ajax call example
 * 
 * <pre>
 *   $.getFeed({
 *    url: getContext()+'/RAjaxRSS/',
 *    type: &quot;get&quot;,
 *    data : {
 *      loadedUrl: rssSites1[id][&quot;url&quot;],
 *      encoding: rssSites1[id][&quot;encoding&quot;]
 *    },
 *    ...});
 * </pre>
 */
public class AjaxServletRSS extends HttpServlet {

  private static final long serialVersionUID = -4380591383319611597L;
  private static final String ENCODING_UTF8 = "UTF-8";
  private static final String ENCODING_ISO = "ISO-8859-1";

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    String enc = getEncodingParameter(req);
    res.setContentType("text/xml;charset=" + enc);
    String loadedUrl = EncodeHelper.transformHtmlCode(req.getParameter("loadedUrl"));
    InputStream rss = null;
    try {
      URL url = new URL(loadedUrl);
      rss = url.openStream();
      ByteStreams.copy(rss, res.getOutputStream());
    } finally {
      Closeables.closeQuietly(rss);
    }
  }

  /**
   * @param req the HttpServletRequest
   * @return encoding parameter from request, UTF-8 encoding if parameter is not correctly set
   */
  private String getEncodingParameter(HttpServletRequest req) {
    String encodingParam = req.getParameter("encoding");
    if (!StringUtil.isDefined(encodingParam)) {
      encodingParam = ENCODING_UTF8;
    } else {
      if (!ENCODING_UTF8.equalsIgnoreCase(encodingParam) &&
          !ENCODING_ISO.equalsIgnoreCase(encodingParam)) {
        encodingParam = ENCODING_UTF8;
      }
    }
    return encodingParam;
  }

}
