/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.web.servlets;

import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.FileResponse;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation
    .startWithTranslationDescriptorPrefix;

/**
 * To get files from temp directory
 *
 * @author neysseri
 */
public class TempFileServer extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 5483484250458795672L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    final String encodedFileName = req.getPathInfo();

    String fileName = StringUtil.EMPTY;
    try {
      fileName = URLDecoder.decode(encodedFileName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throwHttpForbiddenError();
    }

    // Verifying that the path is a valid one (security)
    if (!isValid(fileName)) {
      throwHttpForbiddenError();
    }

    final File physicalFile = new File(FileRepositoryManager.getTemporaryPath(), fileName);
    final SilverpeasFile requestedFile = SilverpeasFileProvider.getFile(physicalFile.getPath());

    if (!requestedFile.exists()) {
      throwHttpNotFoundError();
    }

    try {
      FileResponse.fromServlet(req, res).sendSilverpeasFile(requestedFile);
    } catch (Exception e) {
      throwHttpPreconditionFailedError();
    }
  }

  /**
   * Checks the specified path is valid according to some security rules. For example, check there
   * is no attempt to go up the path to access a forbidden resource.
   * @param path the patch to check.
   * @return true if given path is valid, false otherwise.
   */
  private static boolean isValid(String path) {
    return !path.contains("src/main") && !startWithTranslationDescriptorPrefix(path);
  }
}
