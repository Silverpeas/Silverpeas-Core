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
   * Rustine pour bloquer l'accès au fichier webdav. Attention
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
