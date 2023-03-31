/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.jcr.impl.oak.security;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.plugins.tree.TreeLocation;
import org.apache.jackrabbit.oak.security.authorization.ProviderCtx;
import org.apache.jackrabbit.oak.security.authorization.permission.PermissionUtil;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.Permissions;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.RepositoryPermission;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.TreePermission;
import org.apache.jackrabbit.oak.spi.security.principal.AdminPrincipal;
import org.apache.jackrabbit.oak.spi.security.principal.SystemPrincipal;
import org.apache.jackrabbit.oak.spi.security.principal.SystemUserPrincipal;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.security.AccessContext;
import org.silverpeas.core.jcr.security.SilverpeasUserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.Principal;
import java.util.Objects;
import java.util.Set;

import static org.apache.jackrabbit.oak.spi.security.privilege.PrivilegeConstants.JCR_ALL;
import static org.apache.jackrabbit.oak.spi.security.privilege.PrivilegeConstants.JCR_READ;

/**
 * This provider covers permission validation of a given user in Silverpeas upon read and write
 * access on the JCR content tree. It delegates the validation to the
 * {@link SilverpeasTreePermission} instances.
 * @author mmoquillon
 */
public class SilverpeasPermissionProvider implements PermissionProvider {

  private final User user;
  private final Root root;
  private final ProviderCtx providerCtx;
  private final AccessContext accessContext;
  private Root readOnlyRoot;

  /**
   * Constructs a provider of permissions on the content tree rooted by the given node for the user
   * referred by the specified principals.
   * @param root the root node of the content tree.
   * @param principals the principals identifying the user in Silverpeas.
   */
  SilverpeasPermissionProvider(@Nonnull Root root,
      @Nonnull Set<Principal> principals, @Nonnull ProviderCtx providerCtx) {
    SilverpeasUserPrincipal principal = principals.stream()
        .filter(Objects::nonNull)
        .filter(SilverpeasUserPrincipal.class::isInstance)
        .map(SilverpeasUserPrincipal.class::cast)
        .findFirst()
        .orElseGet(() -> principals.stream()
            .filter(Objects::nonNull)
            .filter(this::isSystemOrAdminPrincipal)
            .findFirst()
            .map(p -> new SilverpeasUserPrincipal(User.getSystemUser()))
            .orElseThrow(() -> new IllegalArgumentException(
                "No principals refer a user in Silverpeas")));
    this.user = principal.getUser();
    this.accessContext = principal.getAccessContext();
    this.root = root;
    this.providerCtx = providerCtx;
    this.readOnlyRoot = this.providerCtx.getRootProvider().createReadOnlyRoot(root);
  }

  @Override
  public void refresh() {
    readOnlyRoot = this.providerCtx.getRootProvider().createReadOnlyRoot(root);
  }

  /**
   * Only the system user and users with administration rights have full access. For any other user,
   * the access is read-only.
   * @param tree The {@code tree} for which the privileges should be retrieved.
   * @return a set of privilege names.
   */
  @Override
  public @Nonnull Set<String> getPrivileges(@Nullable final Tree tree) {
    if (tree == null || user == null) {
      return Set.of();
    }
    if (isSystemOrAdminAccess()) {
      return Set.of(JCR_ALL);
    }
    return Set.of(JCR_READ);
  }

  @Override
  public boolean hasPrivileges(@Nullable final Tree tree, final @Nonnull String... privilegeNames) {
    if (isSystemOrAdminAccess()) {
      return true;
    }
    if (privilegeNames.length == 1) {
      return privilegeNames[0].equals(JCR_READ);
    }
    return false;
  }

  @Override
  public @Nonnull RepositoryPermission getRepositoryPermission() {
    return isSystemOrAdminAccess() ? RepositoryPermission.ALL : RepositoryPermission.EMPTY;
  }

  @Override
  public @Nonnull TreePermission getTreePermission(@Nonnull final Tree tree,
      @Nonnull final TreePermission parentPermission) {
    Tree readOnlyTree = PermissionUtil.getReadOnlyTree(tree, readOnlyRoot);
    return new SilverpeasTreePermission(readOnlyTree, user, accessContext);
  }

  @Override
  public boolean isGranted(@Nonnull final Tree tree, @Nullable final PropertyState property,
      final long permissions) {
    TreePermission treePermission = getTreePermission(tree, TreePermission.NO_RECOURSE);
    return property == null ?
        treePermission.isGranted(permissions) :
        treePermission.isGranted(permissions, property);
  }

  @Override
  public boolean isGranted(@Nonnull final String oakPath, @Nonnull final String jcrActions) {
    if (user.isSystem()) {
      return true;
    }
    TreeLocation location = TreeLocation.create(readOnlyRoot, oakPath);
    long permissions = Permissions.getPermissions(jcrActions, location, false);
    return isGranted(location, permissions);
  }

  private boolean isSystemOrAdminAccess() {
    return user.isSystem() || user.isAccessAdmin();
  }

  private boolean isSystemOrAdminPrincipal(Principal principal) {
    return principal instanceof SystemPrincipal || principal instanceof SystemUserPrincipal ||
        principal instanceof AdminPrincipal;
  }

  private boolean isGranted(@Nonnull TreeLocation location, long permissions) {
    final boolean isGranted;
    PropertyState property = location.getProperty();
    Tree tree = (property == null) ? location.getTree() : location.getParent().getTree();
    if (tree != null) {
      isGranted = isGranted(tree, property, permissions);
    } else {
      isGranted = false;
    }
    return isGranted;
  }

}
