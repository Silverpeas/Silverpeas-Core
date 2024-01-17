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
package org.silverpeas.core.jcr;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * A provider of JCR instances. It aims to provide, through the IoC mechanism, the
 * {@link javax.jcr.Repository} object through which Silverpeas access the JCR. For doing, the
 * {@link RepositoryProvider} instance delegates the getting of such a repository
 * to a {@link javax.jcr.RepositoryFactory} object it gets through the Java Service Provider
 * interface. So, and because it could have more than one such a factory, it selects the correct one
 * by passing some parameters the factory implementation has to understand and to satisfy in order
 * to create the corresponding {@link javax.jcr.Repository}. The parameters and defined in a
 * {@link RepositorySettings} instance.
 * @author mmoquillon
 */
@Provider
public class RepositoryProvider {

  private SilverpeasRepository repository;

  /**
   * Gets an instance of the {@link RepositoryProvider}.
   * @return a {@link RepositoryProvider} instance.
   */
  public static RepositoryProvider get() {
    return ServiceProvider.getSingleton(RepositoryProvider.class);
  }

  @PostConstruct
  private void openRepository() {
    RepositorySettings settings = new RepositorySettings();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RepositorySettings.JCR_HOME, settings.getJCRHomeDirectory());
    parameters.put(RepositorySettings.JCR_CONF, settings.getJCRConfigurationFile());

    Function<RepositoryFactory, Repository> repositoryGetter = f -> {
      try {
        return f.getRepository(parameters);
      } catch (RepositoryException e) {
        throw new SilverpeasRuntimeException(e);
      }
    };
    SilverpeasRepositoryFactory factory = ServiceLoader.load(RepositoryFactory.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(SilverpeasRepositoryFactory.class::isInstance)
        .map(SilverpeasRepositoryFactory.class::cast)
        .findFirst()
        .orElseThrow(() -> new SilverpeasRuntimeException("No JCR backend found!"));

    Repository jcr = Optional.ofNullable(repositoryGetter.apply(factory))
        .orElseThrow(() -> new SilverpeasRuntimeException("No JCR backend found!"));

    SilverLogger.getLogger(this).info("Open connection to the JCR");
    repository = SilverpeasRepository.wrap(jcr);
  }

  @Produces
  public SilverpeasRepository getRepository() {
    return repository;
  }
}
