package com.silverpeas.jcrutil.security;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;

import com.silverpeas.jcrutil.security.impl.RepositoryHelper;

public class ProxyAccessManager implements AccessManager {
  AccessManager realManager;

  public ProxyAccessManager() {
    this.realManager = RepositoryHelper.getJcrAccessManager();
  }

  public boolean canAccess(String workspaceName)
      throws NoSuchWorkspaceException, RepositoryException {
    return this.realManager.canAccess(workspaceName);
  }

  public void checkPermission(ItemId id, int permissions)
      throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    this.realManager.checkPermission(id, permissions);
  }

  public void close() throws Exception {
    this.realManager.close();
  }

  public void init(AMContext context) throws AccessDeniedException, Exception {
    this.realManager.init(context);
  }

  public boolean isGranted(ItemId id, int permissions)
      throws ItemNotFoundException, RepositoryException {
    return this.realManager.isGranted(id, permissions);
  }

}
