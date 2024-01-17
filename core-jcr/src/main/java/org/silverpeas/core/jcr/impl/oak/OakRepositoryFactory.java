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

package org.silverpeas.core.jcr.impl.oak;

import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.jcr.SilverpeasRepositoryFactory;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;
import org.silverpeas.core.jcr.impl.oak.configuration.StorageType;
import org.silverpeas.core.jcr.impl.oak.factories.CompositeNodeStoreFactory;
import org.silverpeas.core.jcr.impl.oak.factories.DocumentNodeStoreFactory;
import org.silverpeas.core.jcr.impl.oak.factories.MemoryNodeStoreFactory;
import org.silverpeas.core.jcr.impl.oak.factories.NodeStoreFactory;
import org.silverpeas.core.jcr.impl.oak.factories.SegmentNodeStoreFactory;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>
 * A factory of a {@link Repository} implemented by Apache Jackrabbit Oak. The factory checks the
 * required parameters are available to take the control of the construction of the
 * {@link Repository} instance. The repository is created by using the Oak API and from the
 * configuration parameters that were loaded from a properties file in the JCR home directory. The
 * expected parameters are:
 * </p>
 * <ul>
 *   <li>{@link RepositorySettings#JCR_HOME}: the absolute path of the JCR home directory,</li>
 *   <li>{@link RepositorySettings#JCR_CONF}: the absolute path of the configuration file in
 *   which are specified the type of repository backend to use with Oak and its configuration
 *   parameters.</li>
 * </ul>
 */
public class OakRepositoryFactory implements SilverpeasRepositoryFactory {

  private final Map<StorageType, Supplier<NodeStoreFactory>>
      nodeStoreFactories = Map.of(
      StorageType.MEMORY_NODE_STORE, MemoryNodeStoreFactory::new,
      StorageType.SEGMENT_NODE_STORE, SegmentNodeStoreFactory::new,
      StorageType.DOCUMENT_NODE_STORE, DocumentNodeStoreFactory::new,
      StorageType.COMPOSITE_NODE_STORE, CompositeNodeStoreFactory::new
  );

  @Override
  public Repository getRepository(final Map parameters) throws RepositoryException {
    try {
      String jcrHomePath = (String) parameters.get(RepositorySettings.JCR_HOME);
      String confPath = (String) parameters.get(RepositorySettings.JCR_CONF);
      if (StringUtil.isNotDefined(jcrHomePath) || StringUtil.isNotDefined(confPath)) {
        return null;
      }

      OakRepositoryConfiguration conf = OakRepositoryConfiguration.load(confPath);
      NodeStoreFactory nodeStoreFactory = nodeStoreFactories.getOrDefault(conf.getStorageType(),
          InvalidNodeStoreFactory::new).get();


      NodeStore nodeStore = nodeStoreFactory.create(jcrHomePath, conf);
      OakRepository repository = OakRepository.create(nodeStore);

      ResourcesCloser.get().register(repository::shutdown);

      return repository;
    } catch (SilverpeasRuntimeException | IOException e) {
      throw new RepositoryException(e);
    }
  }

  private static class InvalidNodeStoreFactory implements NodeStoreFactory {
    @Override
    public NodeStore create(final String jcrHomePath, final OakRepositoryConfiguration conf) {
      SilverLogger.getLogger(this).error("Invalid storage type: " + conf.getStorageType());
      return null;
    }
  }
}
