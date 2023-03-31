/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.webdav;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A servlet taking in charge the access of the content in the JCR repository by WebDAV.
 * <p>
 * The document accessed by WebDAV should be identified uniquely by a tiny URL computed by a
 * {@link WebDavContext} instance from the Web URL locating the document in the JCR. This servlet
 * uses the {@link DavResourceLocatorFactory} to get a resource locator with which the path of the
 * document in the JCR can be figuring out from his tiny WebDAV URL.
 * </p>
 * @author mmoquillon
 */
public class JCRWebDavServlet extends SimpleWebdavServlet {
  private static final long serialVersionUID = -6749255396944745234L;

  @Inject
  private Repository repository;

  @Inject
  private WebDavCredentialsProvider credentialsProvider;

  @Override
  public void init() throws ServletException {
    super.init();
    setLocatorFactory(new DavResourceLocatorFactory(getPathPrefix()));
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
      super.service(new HttpRequestFixer(request), response);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw ex;
    }
  }
}
