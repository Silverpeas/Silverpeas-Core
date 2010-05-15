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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.jcrutil.security.impl;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

public class SimpleAccessManager implements AccessManager {

  @Override
  public boolean canAccess(String workspaceName)
      throws NoSuchWorkspaceException, RepositoryException {
    return true;
  }

  @Override
  public void checkPermission(ItemId id, int permissions)
      throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    return;
  }

  @Override
  public synchronized void close() throws Exception {
    // Nothing to be done
  }

  @Override
  public void init(AMContext context) throws AccessDeniedException, Exception {
    // Nothing to be done

  }

  @Override
  public boolean isGranted(ItemId id, int permissions)
      throws ItemNotFoundException, RepositoryException {
    return true;
  }

  @Override
  public void init(AMContext amc, AccessControlProvider acp, WorkspaceAccessManager wam)
      throws AccessDeniedException, Exception {
  }

  @Override
  public void checkPermission(Path path, int i) throws AccessDeniedException, RepositoryException {
  }

  @Override
  public boolean isGranted(Path path, int i) throws RepositoryException {
    return true;
  }

  @Override
  public boolean isGranted(Path path, Name name, int i) throws RepositoryException {
    return true;
  }

  @Override
  public boolean canRead(Path path) throws RepositoryException {
    return true;
  }

}
