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

import org.apache.jackrabbit.oak.plugins.tree.RootProvider;
import org.apache.jackrabbit.oak.plugins.tree.TreeProvider;
import org.apache.jackrabbit.oak.plugins.tree.impl.RootProviderService;
import org.apache.jackrabbit.oak.plugins.tree.impl.TreeProviderService;
import org.apache.jackrabbit.oak.spi.security.ConfigurationBase;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.SecurityConfiguration;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.security.authentication.AuthenticationConfiguration;
import org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Security provider for the Oak implementation of the JCR. It provides the objects required by Oak
 * to authenticate a user accessing a repository and to authorize him to navigate across the
 * repository's content tree.
 * <p>
 * The security provider provides to Oak both a custom
 * {@link org.apache.jackrabbit.oak.spi.security.authentication.AuthenticationConfiguration} and a
 * custom {@link org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration}
 * objects that defines the rules to apply when authenticating and authorizing a user accessing the
 * JCR repository. Those configuration objects define a bridge between the Oak implementation of the
 * JCR and the Silverpeas world.
 * </p>
 * @author mmoquillon
 */
public class SilverpeasSecurityProvider implements SecurityProvider {

  private final RootProvider rootProvider = new RootProviderService();
  private final TreeProvider treeProvider = new TreeProviderService();
  private final SilverpeasAuthenticationConfiguration authenticationConfig;
  private final SilverpeasAuthorizationConfiguration authorizationConfig;

  public SilverpeasSecurityProvider() {
    this.authenticationConfig =
        initDefaultConfiguration(new SilverpeasAuthenticationConfiguration());
    this.authorizationConfig =
        initDefaultConfiguration(new SilverpeasAuthorizationConfiguration(this));
  }

  @Override
  @Nonnull
  public ConfigurationParameters getParameters(@Nullable final String name) {
    return ConfigurationParameters.EMPTY;
  }

  @Override
  @Nonnull
  public Iterable<? extends SecurityConfiguration> getConfigurations() {
    return Set.of(authenticationConfig, authorizationConfig);
  }

  @Override
  @SuppressWarnings("unchecked")
  @Nonnull
  public <T> T getConfiguration(@Nonnull final Class<T> configClass) {
    if (AuthenticationConfiguration.class == configClass) {
      return (T) authenticationConfig;
    } else if (AuthorizationConfiguration.class == configClass) {
      return (T) authorizationConfig;
    } else {
      throw new IllegalArgumentException("Unsupported security configuration class " + configClass);
    }
  }

  private <T extends SecurityConfiguration> T initDefaultConfiguration(@Nonnull T config) {
    if (config instanceof ConfigurationBase) {
      ConfigurationBase cfg = (ConfigurationBase) config;
      cfg.setRootProvider(rootProvider);
      cfg.setTreeProvider(treeProvider);
    }
    return config;
  }
}
