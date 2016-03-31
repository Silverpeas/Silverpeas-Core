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

package org.silverpeas.core.persistence.jcr;

import org.silverpeas.core.persistence.jcr.provider.JcrSystemCredentialsProvider;
import org.silverpeas.core.util.ServiceProvider;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.text.MessageFormat;

/**
 * A connector in charge to manage the connexions with the underlying JCR repository used by
 * Silverpeas. For each opened connexion, a session is created and from which the content of
 * the repository can be accessed.
 * @author mmoquillon
 */
public class JcrRepositoryConnector {

  private static final String USER_ID_PATTERN = "{0}@domain{1}";

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
    Repository repository = getRepository();
    String userID = MessageFormat.format(USER_ID_PATTERN, login, domainId);
    Credentials credentials = new SimpleCredentials(userID, password.toCharArray());
    return new JcrSession(repository.login(credentials));
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
    Repository repository = getRepository();
    Credentials credentials = JcrSystemCredentialsProvider.getJcrSystemCredentials();
    return new JcrSession(repository.login(credentials));
  }

  public static void closeSession(Session session) {
    if (session != null) {
      session.logout();
    }
  }

  private static final Repository getRepository() {
    JcrRepositoryProvider provider = ServiceProvider.getService(JcrRepositoryProvider.class);
    return provider.getRepository();
  }
}
