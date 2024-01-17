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

package org.silverpeas.core.jcr.security;

import org.apache.jackrabbit.oak.spi.security.authorization.permission.Permissions;
import org.silverpeas.core.admin.user.model.User;

/**
 * The access controller aims to check the authenticated user has the rights to access either for
 * modification or for read the items in the JCR. An item can be either a node or a property of a
 * node. An access controller should be created for each authenticated user. This class centralizes
 * the rules applied in Silverpeas to control such access rights or permissions for a given user and
 * for whatever implementation of the JCR behind the scene. It expects the control has already and
 * actually been done by one of the
 * {@link org.silverpeas.core.security.authorization.AccessController}s in Silverpeas before
 * accessing the items in the JCR; this controller applying just a simple control to ensure the
 * correctness of the permissions on the accessed item of the JCR with this peculiar rule for
 * properties: the permissions on a property of a node is granted if and only if they are granted on
 * the node itself, and this for whatever property of the node.
 * <p>
 * In the JCR, the access rights for each user or for each group of users are stored within the JCR
 * itself. So the default control of the accesses in the implementations of the JCR are built with
 * this characteristic in mind. As for the authentication, in order to avoid deduplication of such a
 * control between Silverpeas and the JCR, the access control in the JCR has to be delegated to
 * Silverpeas; hence this class. The Silverpeas wrapper over the implementation of the JCR in use
 * should either extend this class or instantiate it to perform access controls for the
 * underlying JCR implementation.
 * </p>
 * @author mmoquillon
 */
public class JCRAccessController {

  private final User user;
  private final AccessContext accessContext;

  /**
   * Construct a new access controller for the given user and with the specified context.
   * @param user the user accessing the JCR.
   * @param context the context for which the user accesses the JCR.
   */
  public JCRAccessController(final User user, AccessContext context) {
    this.user = user;
    this.accessContext = context;
  }

  /**
   * Is the specified permissions are granted on the given node to the underlying user? To
   * control the access rights of the user on the properties of a node, this method has to be used
   * with, as parameter, the node having the property on which the permissions have to be checked.
   * Indeed, the permissions on the properties of a node are granted to the user if the same
   * permissions are also granted to user on the node itself.
   * @param node the node accessed by the user.
   * @param permissions the permissions as a bitmask value to check on the node.
   * @return true if the permissions are granted on the node to the user, false otherwise.
   */
  public boolean isGranted(JCRNode<?> node, long permissions) {
    if (user == null) {
      return false;
    }
    if (isSystemOrAdminAccess()) {
      return true;
    }
    boolean granted;
    if (node.isFolder()) {
      granted = isFolderAccessGranted(node, permissions);
    } else if (node.isFile() && node.isLocked()) {
      granted = isFileAccessGranted(node, permissions);
    } else {
      granted = isNodeAccessGranted(node, permissions);
    }
    return granted;
  }

  /**
   * Gets the user for which this controller checks the access.
   * @return the user covered by this access controller.
   */
  public User getUser() {
    return user;
  }

  /**
   * Gets the context under which the user accesses the JCR.
   * @return the context of the user access.
   */
  public AccessContext getAccessContext() {
    return accessContext;
  }

  private boolean isNodeAccessGranted(final JCRNode<?> node, long permissions) {
    return permissions == Permissions.READ || isFolderAccessGranted(node, permissions);
  }

  private boolean isFileAccessGranted(final JCRNode<?> tree, long permissions) {
    User usr = getUser();
    AccessContext ctx = getAccessContext();
    return usr.isAccessAdmin() || tree.isOwnedByUser(usr) ||
        ctx.isGranted(tree.getPath(), permissions);
  }

  private boolean isFolderAccessGranted(final JCRNode<?> tree, long permissions) {
    User usr = getUser();
    AccessContext ctx = getAccessContext();
    return usr.isAccessAdmin() || ctx.isGranted(tree.getPath(), permissions);
  }

  private boolean isSystemOrAdminAccess() {
    User usr = getUser();
    return usr.isSystem() || usr.isAccessAdmin();
  }

}
