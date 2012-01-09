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

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
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
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.rmi.server.RemoteAdapterFactory;
import org.apache.jackrabbit.rmi.server.ServerAdapterFactory;

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
  private transient PeriodicJcrCleaner cleaner;
  private transient RmiConfiguration config;
  private transient String jndiName = DEFAULT_JNDI_NAME;
  /**
   * Keeps a strong reference to the server side RMI repository instance to prevent the RMI
   * distributed Garbage Collector from collecting the instance making the repository unaccessible
   * though it should still be. This field is only set to a non-
   * <code>null</code> value, if registration of the repository to an RMI registry succeeded in the
   */
  private Remote rmiRepository;

  /**
   * Initializes the servlet.<br> Please note that only one repository startup servlet may exist per
   * webapp. it registers itself as context attribute and acts as singleton.
   *
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
      ResourceLocator resources = new ResourceLocator("com.stratelia.webactiv.util.jcr", "");
      repository = (Repository) new InitialContext().lookup(jndiName);
      config = (RmiConfiguration) BasicDaoFactory.getBean("rmi-configuration");
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet.init()", "About to launch cleaner Thread");
      cleaner = new PeriodicJcrCleaner(repository);
      new Thread(cleaner).start();
      SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
          "RepositoryAccessServlet initialized.", repository.toString());
      // registerRMI(config);
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
    } catch (ItemStateException e) {
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
  public void destroy() {
    super.destroy();
    unregisterRMI(config);
    unregisterJNDI();
    log("Closing the repository ...........");
    SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
        "Closing the repository ...........");
  }

  private void unregisterJNDI() {
    try {
      RegistryHelper.unregisterRepository(new InitialContext(), jndiName);
    } catch (NamingException ex) {
      SilverTrace.error("RepositoryAccessServlet", "jackrabbit.unregisterJndi",
          "Unregistering the repository ...........", ex);
    }
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
      throw new ServletException("Erreur avec le repository fourni par Spring",
          rex);
    }
  }

  /**
   * Returns the instance of this servlet
   *
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
   *
   * @return a JCR repository
   * @throws IllegalStateException if the repository is not available in the context.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Returns the JCR repository
   *
   * @param ctx the servlet context
   * @return a JCR repository
   * @throws IllegalStateException if the repository is not available in the context.
   */
  public static Repository getRepository(ServletContext ctx) {
    return getInstance(ctx).getRepository();
  }

  /**
   * Return the fully qualified name of the class providing the remote repository. The class whose
   * name is returned must implement the {@link RemoteFactoryDelegater} interface.
   * <p/>
   * Subclasses may override this method for providing a name of a own implementation.
   *
   * @return getClass().getName() + "$RMIRemoteFactoryDelegater"
   */
  protected String getRemoteFactoryDelegaterClass() {
    return getClass().getName() + "$RMIRemoteFactoryDelegater";
  }

  private void registerRMI(RmiConfiguration config) throws ServletException {
    String rmiUri = "//" + config.getHost() + ":" + config.getPort() + "/" +
        config.getName();
    // try to create remote repository
    Remote remote;
    try {
      Class clazz = Class.forName(getRemoteFactoryDelegaterClass());
      RemoteFactoryDelegater rmf = (RemoteFactoryDelegater) clazz.newInstance();
      remote = rmf.createRemoteRepository(repository);
    } catch (RemoteException e) {
      SilverTrace.error("attachment", "RepositoryAccessServlet",
          "jackrabbit.init", "Unable to create RMI repository.", e);
      throw new ServletException("Unable to create remote repository.", e);
    } catch (NoClassDefFoundError e) {
      throw new ServletException(
          "Unable to create RMI repository. jcr-rmi.jar might be missing.", e);
    } catch (Exception e) {
      SilverTrace.error("attachment", "RepositoryAccessServlet",
          "jackrabbit.init", "Unable to create RMI repository.", e);
      throw new ServletException(
          "Unable to create RMI repository. jcr-rmi.jar might be missing.");
    }

    try {
      System.setProperty("java.rmi.server.useCodebaseOnly", "true");
      Registry reg = null;

      // first try to create the registry, which will fail if another
      // application is already running on the configured host/port
      // or if the rmiHost is not localResourceLocator
      try {
        // find the server socket factory: use the default if the
        // rmiHost is not configured
        RMIServerSocketFactory sf;

        SilverTrace.error("RepositoryAccessServlet", "jackrabbit.init",
            "Creating RMIServerSocketFactory for host " + config.getHost());
        InetAddress hostAddress = InetAddress.getByName(config.getHost());
        sf = getRMIServerSocketFactory(hostAddress);

        // create a registry using the default client socket factory
        // and the server socket factory retrieved above. This also
        // binds to the server socket to the rmiHost:rmiPort.
        reg = LocateRegistry.createRegistry(config.getPort(), null, sf);

      } catch (UnknownHostException uhe) {
        // thrown if the rmiHost cannot be resolved into an IP-Address
        // by getRMIServerSocketFactory
        SilverTrace.error("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "Cannot create Registry", uhe);
      } catch (RemoteException e) {
        // thrown by createRegistry if binding to the rmiHost:rmiPort
        // fails, for example due to rmiHost being remote or another
        // application already being bound to the port
        SilverTrace.error("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "Cannot create Registry", e);
      }

      // if creation of the registry failed, we try to access an
      // potentially active registry. We do not check yet, whether the
      // registry is actually accessible.
      if (reg == null) {
        SilverTrace.info("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "Trying to access existing registry at " +
            config.getHost() + ":" + config.getPort());
        try {
          reg = LocateRegistry.getRegistry(config.getHost(), config.getPort());
        } catch (RemoteException re) {
          SilverTrace.error("attachment", "RepositoryAccessServlet",
              "jackrabbit.init",
              "Cannot create the reference to the registry at " +
              config.getHost() + ":" + config.getPort(), re);
        }
      }

      // if we finally have a registry, register the repository with the
      // rmiName
      if (reg != null) {
        SilverTrace.info("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "Registering repository as " + config.getName() +
            " to registry " + reg);
        reg.bind(config.getName(), remote);

        // when successfull, keep references
        this.rmiRepository = remote;
        SilverTrace.info("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "Repository bound via RMI with name: " + rmiUri);
      } else {
        SilverTrace.error("attachment", "RepositoryAccessServlet",
            "jackrabbit.init", "RMI registry missing, cannot bind repository via RMI");
      }

    } catch (RemoteException e) {
      SilverTrace.error("attachment", "RepositoryAccessServlet",
          "jackrabbit.init", "Unable to bind repository via RMI.", e);
      throw new ServletException("Unable to bind repository via RMI.", e);
    } catch (AlreadyBoundException e) {
      SilverTrace.error("attachment", "RepositoryAccessServlet",
          "jackrabbit.init", "Unable to bind repository via RMI.", e);
      throw new ServletException("Unable to bind repository via RMI.", e);
    }
  }

  /**
   * Unregisters the repository from the RMI registry, if it has previously been registered.
   */
  private void unregisterRMI(RmiConfiguration config) {

    if (rmiRepository != null && config != null) {
      String rmiUri = "//" + config.getHost() + ":" + config.getPort() + "/" +
          config.getName();
      // drop strong reference to remote repository
      rmiRepository = null;
      // unregister repository
      try {
        Naming.unbind(rmiUri);
      } catch (Exception e) {
        log("Error while unbinding repository from JNDI: " + e);
      }
    }
  }

  /**
   * Returns an
   * <code>RMIServerSocketFactory</code> used to create the server socket for a locally created RMI
   * registry.
   * <p/>
   * This implementation returns a new instance of a simple
   * <code>RMIServerSocketFactory</code> which just creates instances of the
   * <code>java.net.ServerSocket</code> class bound to the given
   * <code>hostAddress</code>. Implementations may overwrite this method to provide factory
   * instances, which provide more elaborate server socket creation, such as SSL server sockets.
   *
   * @param hostAddress The
   * <code>InetAddress</code> instance representing the the interface on the localResourceLocator
   * host to which the server sockets are bound.
   * @return A new instance of a simple
   * <code>RMIServerSocketFactory</code> creating
   * <code>java.net.ServerSocket</code> instances bound to the
   * <code>rmiHost</code>.
   */
  protected RMIServerSocketFactory getRMIServerSocketFactory(
      final InetAddress hostAddress) {
    return new RMIServerSocketFactory() {

      @Override
      public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port, -1, hostAddress);
      }
    };
  }

  /**
   * optional class for RMI, will only be used, if RMI server is present
   */
  protected static abstract class RemoteFactoryDelegater {

    public abstract Remote createRemoteRepository(Repository repository) throws RemoteException;
  }

  /**
   * optional class for RMI, will only be used, if RMI server is present
   */
  protected static class RMIRemoteFactoryDelegater extends RemoteFactoryDelegater {

    private static final RemoteAdapterFactory FACTORY = new ServerAdapterFactory();

    @Override
    public Remote createRemoteRepository(Repository repository) throws RemoteException {
      return FACTORY.getRemoteRepository(repository);
    }
  }
}
