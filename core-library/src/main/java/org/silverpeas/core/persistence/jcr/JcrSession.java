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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

/**
 * A JCR session that wraps the actual opened session by adding it the auto-closeable property in
 * order to facilitate its use.
 * @author mmoquillon
 */
public class JcrSession implements AutoCloseable, Session {
  private Session session;

  protected JcrSession(final Session actualSession) {
    this.session = actualSession;
  }

  @Override
  public void close() {
    JcrRepositoryConnector.closeSession(this.session);
  }

  public Repository getRepository() {
    return session.getRepository();
  }

  public void exportSystemView(final String absPath, final OutputStream out,
      final boolean skipBinary, final boolean noRecurse)
      throws IOException, PathNotFoundException, RepositoryException {
    session.exportSystemView(absPath, out, skipBinary, noRecurse);
  }

  public boolean isLive() {
    return session.isLive();
  }

  public String[] getAttributeNames() {
    return session.getAttributeNames();
  }

  public void checkPermission(final String absPath, final String actions)
      throws AccessControlException, RepositoryException {
    session.checkPermission(absPath, actions);
  }

  public ContentHandler getImportContentHandler(final String parentAbsPath, final int uuidBehavior)
      throws PathNotFoundException, ConstraintViolationException, VersionException, LockException,
      RepositoryException {
    return session.getImportContentHandler(parentAbsPath, uuidBehavior);
  }

  public void importXML(final String parentAbsPath, final InputStream in, final int uuidBehavior)
      throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException,
      VersionException, InvalidSerializedDataException, LockException, RepositoryException {
    session.importXML(parentAbsPath, in, uuidBehavior);
  }

  public boolean propertyExists(final String absPath) throws RepositoryException {
    return session.propertyExists(absPath);
  }

  public Node getNodeByIdentifier(final String id)
      throws ItemNotFoundException, RepositoryException {
    return session.getNodeByIdentifier(id);
  }

  public void move(final String srcAbsPath, final String destAbsPath)
      throws ItemExistsException, PathNotFoundException, VersionException,
      ConstraintViolationException, LockException, RepositoryException {
    session.move(srcAbsPath, destAbsPath);
  }

  public boolean nodeExists(final String absPath) throws RepositoryException {
    return session.nodeExists(absPath);
  }

  public String[] getNamespacePrefixes() throws RepositoryException {
    return session.getNamespacePrefixes();
  }

  public String getNamespaceURI(final String prefix)
      throws NamespaceException, RepositoryException {
    return session.getNamespaceURI(prefix);
  }

  public Session impersonate(final Credentials credentials)
      throws LoginException, RepositoryException {
    return session.impersonate(credentials);
  }

  public Workspace getWorkspace() {
    return session.getWorkspace();
  }

  public Object getAttribute(final String name) {
    return session.getAttribute(name);
  }

  public Node getNodeByUUID(final String uuid) throws ItemNotFoundException, RepositoryException {
    return session.getNodeByUUID(uuid);
  }

  public void save()
      throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException,
      ConstraintViolationException, InvalidItemStateException, VersionException, LockException,
      NoSuchNodeTypeException, RepositoryException {
    session.save();
  }

  public boolean hasPermission(final String absPath, final String actions)
      throws RepositoryException {
    return session.hasPermission(absPath, actions);
  }

  public void logout() {
    session.logout();
  }

  public boolean itemExists(final String absPath) throws RepositoryException {
    return session.itemExists(absPath);
  }

  public void addLockToken(final String lt) {
    session.addLockToken(lt);
  }

  public void removeLockToken(final String lt) {
    session.removeLockToken(lt);
  }

  public void removeItem(final String absPath)
      throws VersionException, LockException, ConstraintViolationException, AccessDeniedException,
      RepositoryException {
    session.removeItem(absPath);
  }

  public AccessControlManager getAccessControlManager()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    return session.getAccessControlManager();
  }

  public void exportSystemView(final String absPath, final ContentHandler contentHandler,
      final boolean skipBinary, final boolean noRecurse)
      throws PathNotFoundException, SAXException, RepositoryException {
    session.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
  }

  public Item getItem(final String absPath) throws PathNotFoundException, RepositoryException {
    return session.getItem(absPath);
  }

  public RetentionManager getRetentionManager()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    return session.getRetentionManager();
  }

  public Property getProperty(final String absPath)
      throws PathNotFoundException, RepositoryException {
    return session.getProperty(absPath);
  }

  public void exportDocumentView(final String absPath, final OutputStream out,
      final boolean skipBinary, final boolean noRecurse)
      throws IOException, PathNotFoundException, RepositoryException {
    session.exportDocumentView(absPath, out, skipBinary, noRecurse);
  }

  public String getNamespacePrefix(final String uri)
      throws NamespaceException, RepositoryException {
    return session.getNamespacePrefix(uri);
  }

  public String getUserID() {
    return session.getUserID();
  }

  public void setNamespacePrefix(final String prefix, final String uri)
      throws NamespaceException, RepositoryException {
    session.setNamespacePrefix(prefix, uri);
  }

  public String[] getLockTokens() {
    return session.getLockTokens();
  }

  public void exportDocumentView(final String absPath, final ContentHandler contentHandler,
      final boolean skipBinary, final boolean noRecurse)
      throws PathNotFoundException, SAXException, RepositoryException {
    session.exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
  }

  public boolean hasPendingChanges() throws RepositoryException {
    return session.hasPendingChanges();
  }

  public ValueFactory getValueFactory()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    return session.getValueFactory();
  }

  public Node getNode(final String absPath) throws PathNotFoundException, RepositoryException {
    return session.getNode(absPath);
  }

  public void refresh(final boolean keepChanges) throws RepositoryException {
    session.refresh(keepChanges);
  }

  public boolean hasCapability(final String methodName, final Object target,
      final Object[] arguments) throws RepositoryException {
    return session.hasCapability(methodName, target, arguments);
  }

  public Node getRootNode() throws RepositoryException {
    return session.getRootNode();
  }
}
