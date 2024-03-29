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
package org.silverpeas.web.look;

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    String enc = getEncodingParameter(req);
    res.setContentType("text/xml;charset=" + enc);
    String loadedUrl = WebEncodeHelper.htmlStringToJavaString(req.getParameter("loadedUrl"));
    try {
      URL url = new URL(loadedUrl);
      try (InputStream rss = url.openStream()) {
        IOUtils.copy(rss, res.getOutputStream());
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * @param req the HttpServletRequest
   * @return encoding parameter from request, UTF-8 encoding if parameter is not correctly set
   */
  private String getEncodingParameter(HttpServletRequest req) {
    String encodingParam = req.getParameter("encoding");
    if (!StringUtil.isDefined(encodingParam)) {
      encodingParam = Charsets.UTF_8.toString();
    } else {
      if (!Charsets.UTF_8.toString().equalsIgnoreCase(encodingParam) &&
          !Charsets.ISO_8859_1.toString().equalsIgnoreCase(encodingParam)) {
        encodingParam = Charsets.UTF_8.toString();
      }
    }
    return encodingParam;
  }

}
