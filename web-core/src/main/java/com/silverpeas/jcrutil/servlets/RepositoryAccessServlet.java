/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.IOException;
import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;

/**
 * This Class implements a servlet that is used as unified mechanism to retrieve a jcr repository
 * either through JNDI or RMI.
 */
public class RepositoryAccessServlet extends HttpServlet {

  public static final long serialVersionUID = 1L;
  /**
   * Context parameter name for 'this' instance.
   */
  private final static String CTX_PARAM_THIS = "repository.access.servlet";
  private final static String DEFAULT_JNDI_NAME = "java:jcr/local";
  private final static String JNDI_NAME = "jndi_name";
  /**
   * the repository
   */
  private transient Repository repository;
  private transient String jndiName = DEFAULT_JNDI_NAME;

  /**
   * Initializes the servlet.<br>
   * Please note that only one repository startup servlet may exist per webapp. it registers itself
   * as context attribute and acts as singleton.
   * @throws ServletException if a same servlet is already registered or of another initialization
   * error occurs.
   */
  @Override
  public void init() throws ServletException {
    try {
      if (getServletContext().getInitParameter(JNDI_NAME) != null) {
        jndiName = getServletContext().getInitParameter(JNDI_NAME);
      }
      log("Initializing the repository ...........");
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet.init()", "Initializing the repository ...........");
      // check if servlet is defined twice
      if (getServletContext().getAttribute(CTX_PARAM_THIS) != null) {
        throw new ServletException("Only one repository access servlet allowed per web-app.");
      }
      getServletContext().setAttribute(CTX_PARAM_THIS, this);
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet.init()", "Spring context loaded.");
      repository = (Repository) new InitialContext().lookup(jndiName);
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet initialized.", repository.toString());
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet.init()", "RMI registred");
      getServletContext().setAttribute(Repository.class.getName(), repository);
      registerSilverpeasNodeTypes();
    } catch (NamingException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
    } catch (InvalidNodeTypeDefException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      throw new ServletException(e);
    } catch (IOException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      throw new ServletException(e);
    } catch (RepositoryException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      throw new ServletException(e);
    } catch (ParseException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      throw new ServletException(e);
    } catch (ServletException e) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet error", e);
      throw e;
    }
  }

  private void registerSilverpeasNodeTypes()
      throws RepositoryException, ParseException, IOException, NamespaceException,
      InvalidNodeTypeDefException {
    String cndFileName = this.getClass().getClassLoader().getResource(
        "silverpeas-jcr.txt").getFile().replaceAll("%20", " ");
    SilverpeasRegister.registerNodeTypes(cndFileName);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      Session session = repository.login();
      try {
        String user = session.getUserID();
        String name = repository.getDescriptor(Repository.REP_NAME_DESC);
        SilverTrace.info("jcrUtil", "RepositoryAccessServlet.init()",
            "Logged in as " + user + " to a " + name + " repository.");
      } finally {
        session.logout();
      }
    } catch (RepositoryException rex) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet service.", rex);
      throw new ServletException("Erreur avec le repository fourni par Spring", rex);
    }
  }

  /**
   * Returns the instance of this servlet
   * @param ctx the servlet context
   * @return this servlet
   */
  private static RepositoryAccessServlet getInstance(ServletContext ctx) {
    final RepositoryAccessServlet instance = (RepositoryAccessServlet) ctx.getAttribute(
        CTX_PARAM_THIS);
    if (instance == null) {
      throw new IllegalStateException("No RepositoryAccessServlet instance in ServletContext, " +
          "RepositoryAccessServlet servlet not initialized?");
    }
    return instance;
  }

  /**
   * Returns the JCR repository
   * @return a JCR repository
   * @throws IllegalStateException if the repository is not available in the context.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Returns the JCR repository
   * @param ctx the servlet context
   * @return a JCR repository
   * @throws IllegalStateException if the repository is not available in the context.
   */
  public static Repository getRepository(ServletContext ctx) {
    return getInstance(ctx).getRepository();
  }

}