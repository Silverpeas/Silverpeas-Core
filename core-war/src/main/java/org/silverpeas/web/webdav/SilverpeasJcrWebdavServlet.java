/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.webdav;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.webdav.JcrResourceLocatorFactory;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A servlet taking in charge the access of the content in the JCR repository by WebDAV.
 * @author mmoquillon
 */
public class SilverpeasJcrWebdavServlet extends SimpleWebdavServlet {

  @Inject
  private Repository repository;

  @Inject
  private WebDavCredentialsProvider credentialsProvider;

  @Override
  public void init() throws ServletException {
    super.init();
    setLocatorFactory(new JcrResourceLocatorFactory(getPathPrefix()));
  }

  @Override
  public Repository getRepository() {
    return repository;
  }

  @Override
  protected CredentialsProvider getCredentialsProvider() {
    return credentialsProvider;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      super.service(request, response);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw ex;
    }
  }
}
