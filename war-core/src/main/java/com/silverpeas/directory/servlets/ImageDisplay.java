/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.directory.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class ImageDisplay extends HttpServlet {

  private static final long serialVersionUID = -5179071528136683667L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    ImageProfil profile = new ImageProfil(getAvatar(req));
    InputStream in = null;
    OutputStream out = null;
    try {
      in = profile.getImage();
      out = res.getOutputStream();
      IOUtils.copy(in, out);
    } finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
      if (out != null) {
        IOUtils.closeQuietly(out);
      }
    }
  }

  protected String getAvatar(HttpServletRequest req) {
    int position = req.getPathInfo().lastIndexOf('/');
    return req.getPathInfo().substring(position + 1);
  }
}