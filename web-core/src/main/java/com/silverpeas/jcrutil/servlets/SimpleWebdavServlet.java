package com.silverpeas.jcrutil.servlets;

import javax.jcr.Repository;
import javax.servlet.ServletContext;

import com.stratelia.webactiv.util.ResourceLocator;

public class SimpleWebdavServlet extends
    org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet {
  public String getAuthenticateHeaderValue() {
    ResourceLocator resources = new ResourceLocator("com.stratelia.webactiv.util.jcr", "");
    return "Basic realm=\"" + resources.getString("jcr.authentication.realm") + "\"";
  }

  /**
   * the jcr repository
   */
  private Repository repository;

  /**
   * Returns the <code>Repository</code>. If no repository has been set or
   * created the repository initialized by <code>RepositoryAccessServlet</code>
   * is returned.
   *
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
   *
   * @param repository
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }
 
}
