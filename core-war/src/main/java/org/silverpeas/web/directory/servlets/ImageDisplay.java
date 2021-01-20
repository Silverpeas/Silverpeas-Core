/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.servlets;

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageDisplay extends HttpServlet {

  private static final long serialVersionUID = -5179071528136683667L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    SilverLogger logger = SilverLogger.getLogger(this);
    String avatarPath = getAvatar(req);
    ImageProfil profile = new ImageProfil(avatarPath);
    if (!profile.exist()) {
      logger.warn("The image {0} doesn't exist", avatarPath);
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setContentType(profile.getMimeType());
      res.setHeader("Cache-Control", "no-store"); //HTTP 1.1
      res.setHeader("Pragma", "no-cache");
      res.setDateHeader("Expires", -1);
      try (InputStream in = profile.getImage(); OutputStream out = res.getOutputStream()) {
        IOUtils.copy(in, out);
      } catch (IOException e) {
        logger.error("Error while getting image {0}", avatarPath);
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  protected String getAvatar(HttpServletRequest req) {
    int position = req.getPathInfo().indexOf('/');
    return req.getPathInfo().substring(position + 1);
  }
}