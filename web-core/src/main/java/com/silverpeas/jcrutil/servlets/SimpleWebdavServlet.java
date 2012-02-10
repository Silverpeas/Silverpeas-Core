/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jcrutil.servlets;

import com.silverpeas.jcrutil.security.impl.SilverpeasBasicCredentialsProvider;
import com.stratelia.webactiv.util.ResourceLocator;
import javax.jcr.Repository;
import org.apache.jackrabbit.server.CredentialsProvider;

public class SimpleWebdavServlet extends org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet {

  private static final long serialVersionUID = -1609493516113921269L;
  private static final ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.jcr", "");
  /**
   * the jcr repository
   */
  private Repository repository;

  @Override
  public String getAuthenticateHeaderValue() {
    return "Basic realm=\"" + resources.getString("jcr.authentication.realm") + "\"";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Repository getRepository() {
    if (repository == null) {
      repository = RepositoryAccessServlet.getRepository(getServletContext());
    }
    return repository;
  }

  /**
   * {@inheritDoc}
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CredentialsProvider getCredentialsProvider() {
    return new SilverpeasBasicCredentialsProvider(getInitParameter(INIT_PARAM_MISSING_AUTH_MAPPING));
  }
}
