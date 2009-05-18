package com.silverpeas.jcrutil.security.impl;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;

public class SimpleAccessManager implements AccessManager {

  public boolean canAccess(String workspaceName)
      throws NoSuchWorkspaceException, RepositoryException {
    return true;
  }

  public void checkPermission(ItemId id, int permissions)
      throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    return;
  }

  public synchronized void close() throws Exception {
    // Nothing to be done
  }

  public void init(AMContext context) throws AccessDeniedException, Exception {
    // Nothing to be done

  }

  public boolean isGranted(ItemId id, int permissions)
      throws ItemNotFoundException, RepositoryException {
    return true;
  }

}
