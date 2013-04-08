/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.servlets;

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * AbstractServlet used to send a binary file.
 * Return the content of the warning key in the specified resourceLocator as message in case of error.
 * @author ehugonnet
 */
public abstract class AbstractFileSender extends HttpServlet {

  protected void sendFile(HttpServletResponse response, OnlineFile onlineFile) throws IOException {
    sendFile(response, onlineFile.getContentFile());
  }

  protected void sendFile(HttpServletResponse response, File file) throws IOException {
    response.setContentType(FileUtil.getMimeType(file.getName()));
    response.setHeader("Content-Length", String.valueOf(file.length()));
    SilverTrace.debug("peasUtil", "AbstractFileSender.sendFile()", "root.MSG_GEN_ENTER_METHOD",
        " file: " + file.getAbsolutePath());
    try {
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
      SilverTrace.debug("peasUtil", "AbstractFileSender.sendFile()", "root.MSG_GEN_ENTER_METHOD",
          " File was sent");
    } catch (IOException e) {
      SilverTrace.error("peasUtil", "AbstractFileSender.sendFile", "root.EX_CANT_READ_FILE",
          " file: " + file.getAbsolutePath(), e);
      displayWarningHtmlCode(response);
    }
  }

  protected void sendFile(HttpServletResponse response, String file) throws IOException {
    File downloadedFile = new File(file);
    sendFile(response, downloadedFile);
  }

  protected void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    OutputStream out = res.getOutputStream();
    StringReader sr = new StringReader(getResources().getString("warning"));
    try {
      IOUtils.copy(sr, out);
      out.flush();
    } catch (IOException e) {
      SilverTrace.warn("peasUtil", "AbstractFileSender.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties", e);
    } finally {
      IOUtils.closeQuietly(sr);
    }
  }

  protected abstract ResourceLocator getResources();

}
