/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jcr;

import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.persistence.jcr.provider.JcrSystemCredentialsProvider;
import org.silverpeas.core.thread.concurrent.ReentrantSemaphore;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.util.ResourceLocator.getSettingBundle;

/**
 * A connector in charge to manage the connexions with the underlying JCR repository used by
 * Silverpeas. For each opened connexion, a session is created and from which the content of
 * the repository can be accessed.
 * <p>
 *   Number of thread that are able to use a session at a same time can be managed by setting a
 *   positive value to parameter 'jcr.connection.maxThread' of property file 'org.silverpeas.util
 *   .attachment.Attachment'.<br/>
 *   By default, there is no limitation.
 * </p>
 * @author mmoquillon
 */
public class JcrRepositoryConnector {

  private static final String SESSION_KEY_CACHE = JcrRepositoryConnector.class.getName() + "@SESSION";
  private static final String USER_ID_PATTERN = "{0}@domain{1}";
  private static final SettingBundle settings = getSettingBundle("org.silverpeas.util.attachment.Attachment");
  private static final ReentrantSemaphore semaphore = new ReentrantSemaphore(settings.getInteger("jcr.connection.maxThread", 0));

  private JcrRepositoryConnector() {
    // hidden constructor
  }

  /**
   * Opens a connection with the underlying JCR repository by using a basic authentication in which
   * is specified a pair of user identifier and password. This method of authentication must be used
   * to authenticate a user that which to access directly the content of the JCR repository.
   * @param login the login of the user that wants to access the content of the repository.
   * @param domainId the unique identifier of the domain to which the user belongs.
   * @param password the password associated with the login of the user.
   * @return the session spawned by the authenticated connection.
   * @throws RepositoryException if an error occurs while opening the connection with the
   * repository.
   */
  public static JcrSession openBasicSession(String login, String domainId, String password)
      throws RepositoryException {
    final String userID = MessageFormat.format(USER_ID_PATTERN, login, domainId);
    final Credentials credentials = new SimpleCredentials(userID, password.toCharArray());
    return openSession(credentials);
  }

  /**
   * Opens a system connection with the underlying JCR repository. This way of establishing a
   * connection is reserved to the services in Silverpeas in charge to manage the content stored
   * into the JCR repository.
   * @return the session spawned by the system connection.
   * @throws javax.jcr.RepositoryException if an error occurs while opening a system connection with
   * the repository.
   */
  public static JcrSession openSystemSession() throws RepositoryException {
    return openSession(JcrSystemCredentialsProvider.getJcrSystemCredentials());
  }

  private static JcrSession openSession(final Credentials credentials) throws RepositoryException {
    JcrSession jcrSession = null;
    try {
      final SimpleCache cache = CacheServiceProvider.getThreadCacheService().getCache();
      jcrSession = cache.get(SESSION_KEY_CACHE, JcrSession.class);
      if (jcrSession == null) {
        semaphore.acquire();
        final Repository repository = getRepository();
        jcrSession = new JcrSession(repository.login(credentials));
        cache.put(SESSION_KEY_CACHE, jcrSession);
      } else {
        jcrSession.sessionAlreadyOpened();
        SilverLogger.getLogger(JcrRepositoryConnector.class).debug(() ->
            "JCR session has already been opened");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      performCleanup(jcrSession, e);
    } catch (Exception e) {
      performCleanup(jcrSession, e);
    }
    return jcrSession;
  }

  private static void performCleanup(final JcrSession jcrSession, final Exception e)
      throws RepositoryException {
    if (jcrSession != null) {
      jcrSession.close();
    }
    throw new RepositoryException(e);
  }

  public static void closeSession(Session session) {
    try {
      if (CacheServiceProvider.getThreadCacheService().getCache().remove(SESSION_KEY_CACHE) == null) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        SilverLogger.getLogger(JcrRepositoryConnector.class).error(
          format("{0} -> {1}", "JCR session should exist into thread cache", Stream.of(stackTrace)
              .map(StackTraceElement::toString)
              .collect(Collectors.joining("\n\t")))
        );
      }
      if (session != null) {
        session.logout();
      }
    } finally {
      semaphore.release();
    }
  }

  private static Repository getRepository() {
    JcrRepositoryProvider provider = ServiceProvider.getService(JcrRepositoryProvider.class);
    return provider.getRepository();
  }
}
