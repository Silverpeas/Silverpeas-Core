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
package com.silverpeas.jcrutil.security;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;

import com.silverpeas.jcrutil.security.impl.RepositoryHelper;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

public class ProxyAccessManager implements AccessManager {

  AccessManager realManager;

  public ProxyAccessManager() {
    this.realManager = RepositoryHelper.getJcrAccessManager();
  }

  @Override
  public boolean canAccess(String workspaceName)
      throws NoSuchWorkspaceException, RepositoryException {
    return this.realManager.canAccess(workspaceName);
  }

  @Override
  public void checkPermission(ItemId id, int permissions)
      throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    this.realManager.checkPermission(id, permissions);
  }

  @Override
  public void close() throws Exception {
    this.realManager.close();
  }

  @Override
  public void init(AMContext context) throws AccessDeniedException, Exception {
    this.realManager.init(context);
  }

  @Override
  public boolean isGranted(ItemId id, int permissions)
      throws ItemNotFoundException, RepositoryException {
    return this.realManager.isGranted(id, permissions);
  }

  @Override
  public void init(AMContext context, AccessControlProvider acProvider,
      WorkspaceAccessManager wspAccessManager)
      throws AccessDeniedException, Exception {
    this.realManager.init(context, acProvider, wspAccessManager);
  }

  @Override
  public void checkPermission(Path path, int permissions) throws AccessDeniedException,
      RepositoryException {
    this.realManager.checkPermission(path, permissions);
  }

  @Override
  public boolean isGranted(Path path, int permissions) throws RepositoryException {
    return this.realManager.isGranted(path, permissions);
  }

  @Override
  public boolean isGranted(Path path, Name name, int permissions) throws RepositoryException {
    return this.realManager.isGranted(path, name, permissions);
  }

  @Override
  public boolean canRead(Path path) throws RepositoryException {
    return this.realManager.canRead(path);
  }
}
