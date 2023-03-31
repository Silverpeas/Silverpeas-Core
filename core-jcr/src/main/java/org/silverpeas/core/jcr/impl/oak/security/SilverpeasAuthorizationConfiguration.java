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

import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.namepath.NamePathMapper;
import org.apache.jackrabbit.oak.security.authorization.ProviderCtx;
import org.apache.jackrabbit.oak.security.authorization.monitor.AuthorizationMonitor;
import org.apache.jackrabbit.oak.security.authorization.monitor.AuthorizationMonitorImpl;
import org.apache.jackrabbit.oak.security.authorization.permission.AllPermissionProviderImpl;
import org.apache.jackrabbit.oak.security.authorization.permission.PermissionUtil;
import org.apache.jackrabbit.oak.spi.mount.MountInfoProvider;
import org.apache.jackrabbit.oak.spi.mount.Mounts;
import org.apache.jackrabbit.oak.spi.security.ConfigurationBase;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.restriction.RestrictionProvider;
import org.apache.jackrabbit.oak.stats.StatisticsProvider;

import javax.annotation.Nonnull;
import javax.jcr.security.AccessControlManager;
import java.security.Principal;
import java.util.Set;

/**
 * Configuration defining the authorization mechanism Oak has to apply when a user walks across the
 * content tree of a repository within the context of Silverpeas. The goal is to delegate the
 * authorization on the items of the JCR to Silverpeas without using JAAS, for Silverpeas security
 * isn't built upon JAAS.
 * @author mmoquillon
 */
public class SilverpeasAuthorizationConfiguration extends ConfigurationBase
    implements AuthorizationConfiguration, ProviderCtx {

  private final MountInfoProvider mountInfoProvider = Mounts.defaultMountInfoProvider();

  private final AuthorizationMonitor monitor =
      new AuthorizationMonitorImpl(StatisticsProvider.NOOP);

  public SilverpeasAuthorizationConfiguration(@Nonnull SecurityProvider securityProvider) {
    super(securityProvider, securityProvider.getParameters(NAME));
  }

  @Override
  public @Nonnull MountInfoProvider getMountInfoProvider() {
    return mountInfoProvider;
  }

  @Override
  public @Nonnull AuthorizationMonitor getMonitor() {
    return monitor;
  }

  /**
   * Access control management for the JCR isn't supported by Silverpeas. There is no ACL, nor
   * access control policies defined for the JCR tree.
   * @param root the root node of the JCR
   * @param namePathMapper the mapper between JCR name and JCR implementation name.
   * @return nothing. Throws an {@link UnsupportedOperationException} exception if invoked.
   */
  @Nonnull
  @Override
  public AccessControlManager getAccessControlManager(@Nonnull Root root,
      @Nonnull NamePathMapper namePathMapper) {
    throw new UnsupportedOperationException();
  }

  /**
   * Privileges restrictions management for the JCR isn't supported by Silverpeas. Privileges
   * restrictions is an extension of Oak to the existing JCR access control management.
   * @return nothing. Throws an {@link UnsupportedOperationException} exception if invoked.
   */
  @Nonnull
  @Override
  public RestrictionProvider getRestrictionProvider() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a provider of permissions the specified principals have on the different nodes of the JCR
   * tree rooted at the given node.
   * @param root the root node of the repository content tree.
   * @param workspaceName the name of the JCR workspace the content tree belongs to.
   * @param principals a set of principals referring the Silverpeas user accessing the content
   * tree.
   * @return a {@link PermissionProvider} object whose goal is to delegate the permission resolution
   * to the authorization engine of Silverpeas.
   */
  @Nonnull
  @Override
  public PermissionProvider getPermissionProvider(@Nonnull Root root, @Nonnull String workspaceName,
      @Nonnull Set<Principal> principals) {
    if (PermissionUtil.isAdminOrSystem(principals, getParameters())) {
      return new AllPermissionProviderImpl(root, this);
    }
    return new SilverpeasPermissionProvider(root, principals, this);
  }
}
