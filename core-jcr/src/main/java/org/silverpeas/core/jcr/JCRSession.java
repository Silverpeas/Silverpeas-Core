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

package org.silverpeas.core.jcr;

import org.apache.jackrabbit.api.security.authentication.token.TokenCredentials;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.jcr.security.JCRUserCredentialsProvider;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of the {@link javax.jcr.Session} in Silverpeas. It decorates the real session that
 * was opened for accessing the repository by adding to it auto-closeable trait and reentrant
 * capability. Be aware about the limitation of the hypothesis on which the implementation of this
 * reentrant session is built: it expects only one user session can be opened within a single
 * thread, meaning that any further login attempts along one single thread will return the same
 * session; in the case a login attempt is performed by another user in the same thread, an
 * {@link IllegalStateException} exception is thrown.
 * @author mmoquillon
 */
public class JCRSession implements Session, Closeable {

  private static final String SESSION_KEY_CACHE = JCRSession.class.getSimpleName() + "#SESSION";

  private final Session session;
  private int opened = 1;

  /**
   * Opens a new session with the JCR. If a session has been already opened for the same user within
   * the current thread, then returns it. Otherwise, asks for the specified login mechanism to open
   * a new session. It expects any session opening in a same thread are performed for the same user,
   * otherwise an {@link IllegalStateException} is throw.
   * <p>
   * This method is dedicated to be used by the {@link SilverpeasRepository} instance when a
   * login to the JCR is invoked; the login call is delegated to the {@link JCRSession} with the
   * given {@link JCRLogin} function.
   * @param login the JCR login mechanism from which a new user session is obtained.
   * @return a reentrant JCR session wrapping the real user session with the repository.
   * @throws RepositoryException if an error occurs while opening a new session with the JCR.
   */
  static JCRSession open(final Credentials credentials, final JCRLogin login)
      throws RepositoryException {
    final JCRSession session;
    final Optional<JCRSession> current = getCurrent();
    if (current.isPresent()) {
      String userId = getUserID(credentials);
      session = current.get();
      String sessionUserId = session.getUserID();
      if (!Objects.equals(sessionUserId, userId)) {
        String suid = sessionUserId == null ? "guest" : sessionUserId;
        String uid = userId == null ? "unknown" : userId;
        throw new IllegalStateException("Attempt of " + uid +
            " to log into the repository whereas a session was already opened in the same thread " +
            "by the user " + suid);
      }
    } else {
      //noinspection resource
      session = new JCRSession(login.proceed(credentials));
    }
    return session.open();
  }

  /**
   * Gets the current opened session. If there is no session opened, then nothing is returned.
   * @return optionally a {@link JCRSession} instance.
   */
  private static Optional<JCRSession> getCurrent() {
    SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
    return Optional.ofNullable(cache.get(SESSION_KEY_CACHE, JCRSession.class));
  }

  /**
   * Gets the unique identifier of the user having the specified credentials. If no user matches the
   * given credentials, then null is returned.
   * @param credentials the credentials of a user in Silverpeas.
   * @return either the user behind the specified credentials or null.
   */
  private static String getUserID(final Credentials credentials) {
    User user;
    if (credentials instanceof SimpleCredentials) {
      SimpleCredentials simpleCred = (SimpleCredentials) credentials;
      if (simpleCred.getUserID().equals(JCRUserCredentialsProvider.JCR_SYSTEM_ID)) {
        user = User.getSystemUser();
      } else {
        AuthenticationCredential authCred =
            JCRUserCredentialsProvider.getAuthCredentials((SimpleCredentials) credentials);
        user = authCred != null ? User.provider()
            .getUserByLoginAndDomainId(authCred.getLogin(), authCred.getDomainId()) : null;
      }
    } else if (credentials instanceof TokenCredentials) {
      user = User.provider().getUserByToken(((TokenCredentials) credentials).getToken());
    } else {
      user = null;
    }
    return user == null ? null : user.getId();
  }

  /**
   * Opens a session to the JCR for the system user (id est for Silverpeas with administrative
   * rights). If a session already exists, then just returns it. Otherwise, a new session is
   * opened.
   * <p>
   * This is a shortcut of:
   * <pre><code>
   *   Repository repository = RepositoryProvider.get().getRepository();
   *   Credentials credentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
   *   Session session = repository.login(credentials);
   * </code></pre>
   * @return a session
   * @throws RepositoryException if the session opening fails.
   */
  @SuppressWarnings("unused")
  public static JCRSession openSystemSession() throws RepositoryException {
    SilverpeasRepository repo = RepositoryProvider.get().getRepository();
    Credentials credentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
    return repo.login(credentials);
  }

  /**
   * Opens a session to the JCR for the specified user. If a session already exists, then just
   * returns it. Otherwise, a new session is opened. It expects any session opening in a same thread
   * are performed for the same user, otherwise an {@link IllegalStateException} is throw.
   * <p>
   * This is a shortcut of:
   * <pre><code>
   *   Repository repository = RepositoryProvider.get().getRepository();
   *   Credentials credentials = JCRUserCredentialsProvider.getUserCredentials(login, domainId, password);
   *   Session session = repository.login(credentials);
   * </code></pre>
   * @return a session
   * @throws RepositoryException if the session opening fails.
   */
  @SuppressWarnings("unused")
  public static JCRSession openUserSession(String login, String domainId, String password)
      throws RepositoryException {
    SilverpeasRepository repo = RepositoryProvider.get().getRepository();
    Credentials credentials =
        JCRUserCredentialsProvider.getUserCredentials(login, domainId, password);
    return repo.login(credentials);
  }

  /**
   * Creates a new reentrant session decorating the specified true one. If a session is already
   * opened, then an {@link IllegalStateException} is thrown. The created session isn't yet opened:
   * yoy have to call specifically {@link JCRSession#open()} for doing.
   * @param session the new non-reentrant session with the JCR.
   */
  private JCRSession(final Session session) {
    getCurrent().ifPresent(s -> {
      throw new IllegalStateException("A session is already opened!");
    });
    this.session = session;
  }

  @Override
  public Repository getRepository() {
    return RepositoryProvider.get().getRepository();
  }

  /**
   * The unique identifier of the user in Silverpeas who has opened this session.
   * @return the user identifier
   */
  @Override
  public String getUserID() {
    return session.getUserID();
  }

  @Override
  public String[] getAttributeNames() {
    return session.getAttributeNames();
  }

  @Override
  public Object getAttribute(final String name) {
    return session.getAttribute(name);
  }

  @Override
  public Workspace getWorkspace() {
    return session.getWorkspace();
  }

  @Override
  public Node getRootNode() throws RepositoryException {
    return session.getRootNode();
  }

  @Override
  public Session impersonate(final Credentials credentials)
      throws RepositoryException {
    return session.impersonate(credentials);
  }

  @Override
  public Node getNodeByUUID(final String uuid) throws RepositoryException {
    return session.getNodeByIdentifier(uuid);
  }

  @Override
  public Node getNodeByIdentifier(final String id)
      throws RepositoryException {
    return session.getNodeByIdentifier(id);
  }

  @Override
  public Item getItem(final String absPath) throws RepositoryException {
    return session.getItem(absPath);
  }

  @Override
  public Node getNode(final String absPath) throws RepositoryException {
    return session.getNode(absPath);
  }

  @Override
  public Property getProperty(final String absPath)
      throws RepositoryException {
    return session.getProperty(absPath);
  }

  @Override
  public boolean itemExists(final String absPath) throws RepositoryException {
    return session.itemExists(absPath);
  }

  @Override
  public boolean nodeExists(final String absPath) throws RepositoryException {
    return session.nodeExists(absPath);
  }

  @Override
  public boolean propertyExists(final String absPath) throws RepositoryException {
    return session.propertyExists(absPath);
  }

  @Override
  public void move(final String srcAbsPath, final String destAbsPath)
      throws RepositoryException {
    session.move(srcAbsPath, destAbsPath);
  }

  @Override
  public void removeItem(final String absPath)
      throws RepositoryException {
    session.removeItem(absPath);
  }

  @Override
  public void save()
      throws RepositoryException {
    session.save();
  }

  @Override
  public void refresh(final boolean keepChanges) throws RepositoryException {
    session.refresh(keepChanges);
  }

  @Override
  public boolean hasPendingChanges() throws RepositoryException {
    return session.hasPendingChanges();
  }

  @Override
  public ValueFactory getValueFactory()
      throws RepositoryException {
    return session.getValueFactory();
  }

  @Override
  public boolean hasPermission(final String absPath, final String actions)
      throws RepositoryException {
    return session.hasPermission(absPath, actions);
  }

  @Override
  public void checkPermission(final String absPath, final String actions)
      throws AccessControlException, RepositoryException {
    session.checkPermission(absPath, actions);
  }

  @Override
  public boolean hasCapability(final String methodName, final Object target,
      final Object[] arguments) throws RepositoryException {
    return session.hasCapability(methodName, target, arguments);
  }

  @Override
  public ContentHandler getImportContentHandler(final String parentAbsPath, final int uuidBehavior)
      throws RepositoryException {
    return session.getImportContentHandler(parentAbsPath, uuidBehavior);
  }

  @Override
  public void importXML(final String parentAbsPath, final InputStream in, final int uuidBehavior)
      throws IOException, RepositoryException {
    session.importXML(parentAbsPath, in, uuidBehavior);
  }

  @Override
  public void exportSystemView(final String absPath, final ContentHandler contentHandler,
      final boolean skipBinary, final boolean noRecurse)
      throws SAXException, RepositoryException {
    session.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
  }

  @Override
  public void exportSystemView(final String absPath, final OutputStream out,
      final boolean skipBinary, final boolean noRecurse)
      throws IOException, RepositoryException {
    session.exportSystemView(absPath, out, skipBinary, noRecurse);
  }

  @Override
  public void exportDocumentView(final String absPath, final ContentHandler contentHandler,
      final boolean skipBinary, final boolean noRecurse)
      throws SAXException, RepositoryException {
    session.exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
  }

  @Override
  public void exportDocumentView(final String absPath, final OutputStream out,
      final boolean skipBinary, final boolean noRecurse)
      throws IOException, RepositoryException {
    session.exportDocumentView(absPath, out, skipBinary, noRecurse);
  }

  @Override
  public void setNamespacePrefix(final String prefix, final String uri)
      throws RepositoryException {
    session.setNamespacePrefix(prefix, uri);
  }

  @Override
  public String[] getNamespacePrefixes() throws RepositoryException {
    return session.getNamespacePrefixes();
  }

  @Override
  public String getNamespaceURI(final String prefix)
      throws RepositoryException {
    return session.getNamespaceURI(prefix);
  }

  @Override
  public String getNamespacePrefix(final String uri)
      throws RepositoryException {
    return session.getNamespacePrefix(uri);
  }

  @Override
  public void logout() {
    if (opened <= 1) {
      SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
      cache.remove(SESSION_KEY_CACHE);
      session.logout();
    } else {
      opened--;
    }
  }

  @Override
  public boolean isLive() {
    return session.isLive();
  }

  @Override
  public void addLockToken(final String lt) {
    try {
      session.getWorkspace().getLockManager().addLockToken(lt);
    } catch (RepositoryException e) {
      SilverLogger.getLogger(this).error("Unable to add lock token " + lt + " to session", e);
    }
  }

  @Override
  public String[] getLockTokens() {
    try {
      return session.getWorkspace().getLockManager().getLockTokens();
    } catch (RepositoryException e) {
      SilverLogger.getLogger(this).error("Unable to retrieve lock tokens from session", e);
      return new String[0];
    }
  }

  @Override
  public void removeLockToken(final String lt) {
    try {
      session.getWorkspace().getLockManager().removeLockToken(lt);
    } catch (RepositoryException e) {
      SilverLogger.getLogger(this).error("Unable to remove lock token " + lt + " from session", e);
    }
  }

  @Override
  public AccessControlManager getAccessControlManager()
      throws RepositoryException {
    return session.getAccessControlManager();
  }

  @Override
  public RetentionManager getRetentionManager()
      throws RepositoryException {
    return session.getRetentionManager();
  }

  @Override
  public void close() {
    logout();
  }

  /**
   * Opens this session. If the session has been already opened, increment its counter of opening.
   */
  private JCRSession open() {
    getCurrent().ifPresentOrElse(s -> opened++, () -> {
      SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
      cache.put(SESSION_KEY_CACHE, this);
    });
    return this;
  }

  /**
   * Login function that embeds the actual login mechanism or call to the JCR. To be used by the
   * {@link SilverpeasRepository} instances.
   */
  @FunctionalInterface
  interface JCRLogin {
    Session proceed(final Credentials credentials) throws RepositoryException;
  }
}
