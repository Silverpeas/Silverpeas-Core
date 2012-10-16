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

package com.stratelia.webactiv.jaas;

import com.silverpeas.jcrutil.JcrConstants;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemPrincipal;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.security.auth.Subject;
import java.util.Set;

public class SilverpeasAccessManager implements AccessManager {

  private HierarchyManager manager;
  private NamePathResolver resolver;
  private WorkspaceAccessManager wspAccessMgr;
  private PrivilegeRegistry privilegeRegistry;
  private Subject subject;
  private boolean initialized;
  private boolean isSystem = false;
  private Repository repository;

  @Override
  public boolean canAccess(String workspaceName) throws NoSuchWorkspaceException,
      RepositoryException {
    if (isSystem || wspAccessMgr == null) {
      return true;
    }
    return wspAccessMgr.grants(subject.getPrincipals(), workspaceName);
  }

  @Override
  public void checkPermission(ItemId id, int permissions) throws AccessDeniedException,
      ItemNotFoundException, RepositoryException {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    if (!isGranted(id, permissions)) {
      throw new AccessDeniedException();
    }
  }

  @Override
  public synchronized void close() throws Exception {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    initialized = false;
  }

  @Override
  public void init(AMContext context) throws AccessDeniedException, Exception {
    if (initialized) {
      throw new IllegalStateException("already initialized");
    }
    this.manager = context.getHierarchyManager();
    this.resolver = context.getNamePathResolver();
    this.privilegeRegistry = new PrivilegeRegistry(resolver);
    this.subject = context.getSubject();
    this.isSystem = !subject.getPrincipals(SilverpeasSystemPrincipal.class).isEmpty();
    this.initialized = true;
  }

  @Override
  public boolean isGranted(ItemId id, int permissions) throws ItemNotFoundException,
      RepositoryException {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    if (id.denotesNode() && !isSystem) {
      Path path = manager.getPath(id);
      if (path.getDepth() > 2 && validateNode(path)) {
        return isPathAutorized(path);
      } else if (validateFileNode(id)) {
        Set<SilverpeasUserPrincipal> principals =
            subject.getPrincipals(SilverpeasUserPrincipal.class);
        for (SilverpeasUserPrincipal principal : principals) {
          if (checkUserIsOwner(principal, id)) {
            return true;
          }
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Rustine
   * @param principal
   * @param id
   * @return
   * @throws RepositoryException
   */
  protected boolean checkUserIsOwner(SilverpeasUserPrincipal principal, ItemId id)
      throws RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node node = getNode(session, id);
      if (node.hasProperty(JcrConstants.SLV_PROPERTY_OWNER)) {
        return principal.getUserId().equals(
            node.getProperty(JcrConstants.SLV_PROPERTY_OWNER).getValue().getString());
      }
      return true;
    } catch (ItemNotFoundException ex) {
      // The node doesn't exist so we may assume that it is transient in the
      // user's session
      return true;
    } catch (RepositoryException ex) {
      throw ex;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  /**
   * Rustine
   * @param principal
   * @param path
   * @return
   * @throws RepositoryException
   */
  protected boolean checkUserIsOwner(SilverpeasUserPrincipal principal, Path path)
      throws RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node node = getNode(session, path);
      return principal.getUserId().equals(
          node.getProperty(JcrConstants.SLV_PROPERTY_OWNER).getValue().getString());
    } catch (ItemNotFoundException ex) {
      // The node doesn't exist so we may assume that it is transient in the
      // user's session
      return true;
    } catch (RepositoryException ex) {
      throw ex;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected boolean isPathAutorized(Path path) {
    Set<SilverpeasUserPrincipal> principals = subject.getPrincipals(SilverpeasUserPrincipal.class);
    Path.Element[] elements = path.getElements();
    for (SilverpeasUserPrincipal principal : principals) {
      for (Path.Element element : elements) {
        if (principal.getUserProfile(element.getName().getLocalName()) != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Rustine pour bloquer l'acces au fichier webdav. Attention
   * @param id
   * @return
   * @throws LoginException
   * @throws ItemNotFoundException
   * @throws RepositoryException
   */
  protected boolean validateFileNode(ItemId id) throws LoginException, ItemNotFoundException,
      RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node node = getNode(session, id);
      return validateFileNode(node);
    } catch (RepositoryException ex) {
      throw ex;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected boolean validateNode(Path path) throws LoginException, ItemNotFoundException,
      RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node node = getNode(session, path);
      return validateNode(node);
    } catch (RepositoryException ex) {
      throw ex;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected boolean validateNode(Node node) throws RepositoryException {
    return node.getPrimaryNodeType().isNodeType(JcrConstants.NT_FOLDER);
  }

  /**
   * Rustine pour bloquer l'acces au fichier webdav. Attention
   * @param path
   * @return
   * @throws LoginException
   * @throws ItemNotFoundException
   * @throws RepositoryException
   */
  protected boolean validateFileNode(Path path) throws LoginException, ItemNotFoundException,
      RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node node = getNode(session, path);
      return validateFileNode(node);
    } catch (RepositoryException rex) {
      return false;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected boolean validateFileNode(Node node) throws RepositoryException {
    if (JcrConstants.NT_FILE.equals(node.getPrimaryNodeType().getName())) {
      NodeType[] mixins = node.getMixinNodeTypes();
      for (NodeType mixin : mixins) {
        if (JcrConstants.SLV_OWNABLE_MIXIN.equals(mixin.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  @Override
  public void init(AMContext context, AccessControlProvider acProvider,
      WorkspaceAccessManager wspAccessManager) throws AccessDeniedException, Exception {
    if (initialized) {
      throw new IllegalStateException("already initialized");
    }
    this.manager = context.getHierarchyManager();
    this.resolver = context.getNamePathResolver();
    this.privilegeRegistry = new PrivilegeRegistry(resolver);
    this.wspAccessMgr = wspAccessManager;
    this.subject = context.getSubject();
    this.isSystem = !subject.getPrincipals(SilverpeasSystemPrincipal.class).isEmpty();
    this.initialized = true;
  }

  @Override
  public void checkPermission(Path absPath, int permissions) throws AccessDeniedException,
      RepositoryException {
    if (!isGranted(absPath, permissions)) {
      throw new AccessDeniedException("Access denied");
    }
  }

  @Override
  public boolean isGranted(Path path, int permissions) throws RepositoryException {
    if (!isSystem && denotesNode(path)) {
      if (path.getDepth() > 2 && validateNode(path)) {
        return isPathAutorized(path);
      } else if (validateFileNode(path)) {
        Set<SilverpeasUserPrincipal> principals =
            subject.getPrincipals(SilverpeasUserPrincipal.class);
        for (SilverpeasUserPrincipal principal : principals) {
          if (checkUserIsOwner(principal, path)) {
            return true;
          }
        }
        return false;
      }
    }
    return true;

  }

  protected boolean denotesNode(Path path) throws NamespaceException {
    String relativePath = getRelativePath(path);
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      Node root = session.getRootNode();
      if (path.denotesRoot()) {
        return true;
      }
      if (root.hasNode(relativePath)) {
        return true;
      }
      return false;
    } catch (RepositoryException ex) {
      return false;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected Node getNode(Session session, Path path) throws RepositoryException {
    String relativePath = getRelativePath(path);
    Node root = session.getRootNode();
    if (path.denotesRoot()) {
      return root;
    }
    if (root.hasNode(relativePath)) {
      return root.getNode(relativePath);
    }
    return null;

  }

  protected Node getNode(Session session, ItemId id) throws RepositoryException {
    NodeId nodeId = (NodeId) id;
    return session.getNodeByIdentifier(id.toString());
  }

  protected String getRelativePath(Path path) throws NamespaceException {
    String result = this.resolver.getJCRPath(path);
    if (result.startsWith("/")) {
      result = result.substring(1);
    }
    return result;
  }

  @Override
  public boolean isGranted(Path path, Name name, int i) throws RepositoryException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean canRead(Path path, ItemId itemid) throws RepositoryException {
    boolean canAccessPath = true;
    if (path != null) {
      canAccessPath = isGranted(path, Permission.READ);
    }
    return canAccessPath && isGranted(itemid, Permission.READ);
  }

  @Override
  public void checkRepositoryPermission(int permissions) throws AccessDeniedException,
          RepositoryException {
  }
}
