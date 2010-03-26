/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.jcrutil.servlets;

import javax.jcr.Repository;

import com.stratelia.webactiv.util.ResourceLocator;

public class SimpleWebdavServlet extends org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet {
  public String getAuthenticateHeaderValue() {
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.webactiv.util.jcr", "");
    return "Basic realm=\"" + resources.getString("jcr.authentication.realm")
        + "\"";
  }

  /**
   * the jcr repository
   */
  private Repository repository;

  /**
   * Returns the <code>Repository</code>. If no repository has been set or created the repository
   * initialized by <code>RepositoryAccessServlet</code> is returned.
   * @return repository
   * @see RepositoryAccessServlet#getRepository(ServletContext)
   */
  public Repository getRepository() {
    if (repository == null) {
      repository = RepositoryAccessServlet.getRepository(getServletContext());
    }
    return repository;
  }

  /**
   * Sets the <code>Repository</code>.
   * @param repository
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

}
