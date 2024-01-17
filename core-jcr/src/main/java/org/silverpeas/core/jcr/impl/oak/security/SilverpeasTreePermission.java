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

package org.silverpeas.core.jcr.impl.oak.security;

import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.plugins.tree.TreeUtil;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.Permissions;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.TreePermission;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.security.AccessContext;
import org.silverpeas.core.jcr.security.JCRAccessController;
import org.silverpeas.core.jcr.security.JCRNode;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of the {@link TreePermission} interface for Silverpeas. All the permissions
 * checking is centralized within this class and hence performed by its instances. Currently, the
 * objects of this class expect the access control has been done previously by Silverpeas itself, so
 * they have just to ensure the user access level and validate their access context.
 * @author mmoquillon
 */
class SilverpeasTreePermission extends JCRAccessController implements TreePermission {

  private final JCRTree tree;

  SilverpeasTreePermission(final Tree tree, final User user, final AccessContext context) {
    super(user, context);
    this.tree = new JCRTree(tree);
  }

  @Override
  public @Nonnull TreePermission getChildPermission(
      @Nonnull final String childName,
      @Nonnull final NodeState childState) {
    Tree child = tree.getChild(childName);
    return new SilverpeasTreePermission(child, getUser(), getAccessContext());
  }

  @Override
  public boolean canRead() {
    return isGranted(Permissions.READ);
  }

  @Override
  public boolean canRead(@Nonnull final PropertyState property) {
    return isGranted(Permissions.READ, property);
  }

  @Override
  public boolean canReadAll() {
    return isGranted(Permissions.READ | Permissions.READ_ACCESS_CONTROL);
  }

  @Override
  public boolean canReadProperties() {
    return isGranted(Permissions.READ);
  }

  @Override
  public boolean isGranted(final long permissions) {
    return isGranted(tree, permissions);
  }

  @Override
  public boolean isGranted(final long permissions,
      @Nonnull final PropertyState property) {
    // property access is granted only if its holder access is granted
    return isGranted(permissions);
  }

  private static class JCRTree extends JCRNode<Tree> {

    JCRTree(Tree tree) {
      super(tree);
    }
    @Override
    public String getPath() {
      return getNode().getPath();
    }

    @Override
    public String getPrimaryType() {
      return TreeUtil.getPrimaryTypeName(getNode());
    }

    @Override
    public Stream<String> getMixinTypes() {
      Iterable<String> mixins = TreeUtil.getMixinTypeNames(getNode());
      return StreamSupport.stream(mixins.spliterator(), false);
    }

    @Override
    public String getProperty(final String name, final int type) {
      PropertyState prop = getNode().getProperty(name);
      Type<?> propType = Type.fromTag(type, false);
      return prop != null && prop.getType() == propType ? prop.getValue(propType).toString() : null;
    }

    public Tree getChild(final String childName) {
      return getNode().getChild(childName);
    }
  }
}
