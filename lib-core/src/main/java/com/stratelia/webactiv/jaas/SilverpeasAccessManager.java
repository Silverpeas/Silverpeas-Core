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
package com.stratelia.webactiv.jaas;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.security.auth.Subject;

import com.silverpeas.jcrutil.JcrConstants;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.spi.Path;

import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemPrincipal;

public class SilverpeasAccessManager implements AccessManager {

  private HierarchyManager manager;
  private Subject subject;
  private boolean initialized;
  private boolean isSystem = false;
  private Repository repository;

  public boolean canAccess(String workspaceName)
      throws NoSuchWorkspaceException, RepositoryException {
    return true;
  }

  public void checkPermission(ItemId id, int permissions)
      throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    if (!isGranted(id, permissions)) {
      throw new AccessDeniedException();
    }
    return;
  }

  public synchronized void close() throws Exception {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    initialized = false;
  }

  public void init(AMContext context) throws AccessDeniedException, Exception {
    if (initialized) {
      throw new IllegalStateException("already initialized");
    }
    this.manager = context.getHierarchyManager();
    this.subject = context.getSubject();
    this.isSystem = !subject.getPrincipals(SilverpeasSystemPrincipal.class)
        .isEmpty();
    this.initialized = true;
  }

  public boolean isGranted(ItemId id, int permissions)
      throws ItemNotFoundException, RepositoryException {
    if (!initialized) {
      throw new IllegalStateException("not initialized");
    }
    if (id.denotesNode() && !isSystem) {
      Path path = manager.getPath(id);
      if (path.getDepth() > 2 && validateNode(id)) {
        Set principals = subject.getPrincipals(SilverpeasUserPrincipal.class);
        Path.Element[] elements = path.getElements();
        Iterator iter = principals.iterator();
        while (iter.hasNext()) {
          SilverpeasUserPrincipal principal = (SilverpeasUserPrincipal) iter
              .next();
          for (int i = 0; i < elements.length; i++) {
            if (principal.getUserProfile(elements[i].getName().getLocalName()) != null) {
              return true;
            }
          }
        }
        return false;
      } else if (validateFileNode(id)) {
        Set principals = subject.getPrincipals(SilverpeasUserPrincipal.class);
        Iterator iter = principals.iterator();
        while (iter.hasNext()) {
          SilverpeasUserPrincipal principal = (SilverpeasUserPrincipal) iter
              .next();
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
   * 
   * @param principal
   * @param id
   * @return
   * @throws RepositoryException
   */
  protected boolean checkUserIsOwner(SilverpeasUserPrincipal principal,
      ItemId id) throws RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      NodeId nodeId = (NodeId) id;
      Node node = session.getNodeByUUID(nodeId.getUUID().toString());
      return principal.getUserId().equals(
          node.getProperty(JcrConstants.SLV_PROPERTY_OWNER).getValue()
              .getString());
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

  protected boolean validateNode(ItemId id) throws LoginException,
      ItemNotFoundException, RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      NodeId nodeId = (NodeId) id;
      Node node = session.getNodeByUUID(nodeId.getUUID().toString());
      return JcrConstants.NT_FOLDER.equals(node.getPrimaryNodeType());
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
   * Rustine pour bloquer l'acces au fichier webdav. Attention
   * 
   * @param id
   * @return
   * @throws LoginException
   * @throws ItemNotFoundException
   * @throws RepositoryException
   */
  protected boolean validateFileNode(ItemId id) throws LoginException,
      ItemNotFoundException, RepositoryException {
    Session session = null;
    try {
      session = repository.login(new SilverpeasSystemCredentials());
      NodeId nodeId = (NodeId) id;
      Node node = session.getNodeByUUID(nodeId.getUUID().toString());
      if (JcrConstants.NT_FILE.equals(node.getPrimaryNodeType().getName())) {
        NodeType[] mixins = node.getMixinNodeTypes();
        for (int i = 0; i < mixins.length; i++) {
          if (JcrConstants.SLV_OWNABLE_MIXIN.equals(mixins[i].getName())) {
            return true;
          }
        }
        return false;
      }
      return false;
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

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

}
