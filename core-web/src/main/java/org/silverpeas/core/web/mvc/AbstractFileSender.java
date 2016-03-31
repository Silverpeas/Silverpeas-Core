/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.mvc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * AbstractServlet used to send a binary file.
 * Return the content of the warning key in the specified resourceLocator as message in case of error.
 * @author ehugonnet
 */
public abstract class AbstractFileSender extends HttpServlet {


  protected void sendFile(HttpServletResponse response, SilverpeasFile file) throws IOException {
    if (file != null && file.exists() && file.isFileSecure()) {
      response.setContentType(file.getMimeType());
      response.setHeader("Content-Length", String.valueOf(file.length()));
      try {
        FileUtils.copyFile(file, response.getOutputStream());
        response.getOutputStream().flush();
      } catch (IOException e) {
        SilverLogger.getLogger(this).error("file: " + file.getAbsolutePath(), e);
        displayWarningHtmlCode(response);
      }
    } else {
      response.setHeader("Content-Length", "0");
      if (file != null && !file.isFileSecure()) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
      response.getOutputStream().flush();
    }
  }

  protected void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    OutputStream out = res.getOutputStream();
    StringReader sr = new StringReader(getSettingBunde().getString("warning"));
    try {
      IOUtils.copy(sr, out);
      out.flush();
    } catch (IOException e) {
      SilverLogger.getLogger(this).error("warning properties", e);
    } finally {
      IOUtils.closeQuietly(sr);
    }
  }

  protected abstract SettingBundle getSettingBunde();

}
