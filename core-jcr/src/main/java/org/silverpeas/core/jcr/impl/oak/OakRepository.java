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

package org.silverpeas.core.jcr.impl.oak;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.jcr.impl.oak.security.SilverpeasSecurityProvider;
import org.silverpeas.core.jcr.SilverpeasRepository;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;
import org.silverpeas.core.jcr.impl.oak.factories.NodeStoreFactory;

import javax.jcr.Repository;

/**
 * <p>
 * Represents a repository in Oak. In the JCR API, a repository encapsulates the way the JCR is
 * accessed and in Oak it wraps the node storage used to handle the content of the JCR. There are
 * different kinds of storage. Each of them taken in charge by a dedicated {@link NodeStoreFactory}
 * class. Hence, in Oak, the connection to a JCR is established by using such a
 * {@link NodeStoreFactory} instance and the content of the JCR is accessed through the
 * {@link NodeStore} object created by the factory. Disconnecting the JCR means using the same
 * {@link NodeStoreFactory} object to dispose the {@link NodeStore} object used by the repository.
 * For doing, a peculiar class, {@link OakRepositoryConnection}, manages this for the
 * {@link OakRepository} instance and this instance keeps the {@link OakRepositoryConnection}
 * instance that has been used to create it.
 * @author mmoquillon
 */
public class OakRepository extends SilverpeasRepository {

  private final OakRepositoryConnection connection;

  /**
   * Creates a connection link with the JCR repository by using the specified
   * {@link NodeStoreFactory} object to open the storage used by this repository.
   * @param nodeStoreFactory a {@link NodeStoreFactory} object.
   * @return an {@link OakRepositoryConnection} instance with which the repository can be connected
   * and disconnected.
   */
  static OakRepositoryConnection createConnection(final NodeStoreFactory nodeStoreFactory) {
    return new OakRepositoryConnection(nodeStoreFactory);
  }

  private OakRepository(final OakRepositoryConnection connection, final Repository repository) {
    super(repository);
    this.connection = connection;
  }

  public void shutdown() {
    Repository repository = getRepository();
    if (repository instanceof JackrabbitRepository) {
      ((JackrabbitRepository) repository).shutdown();
    }
    connection.disconnect();
  }

  /**
   * A connection with a repository by using the Oak API. The connection uses the service of the
   * {@link NodeStoreFactory} object to both open and close the storage used as backend by the
   * repository. The storage is here represented by a {@link NodeStore} object.
   */
  static class OakRepositoryConnection {

    private final NodeStoreFactory factory;
    private NodeStore nodeStore;

    private OakRepositoryConnection(final NodeStoreFactory nodeStoreFactory) {
      this.factory = nodeStoreFactory;
    }

    /**
     * Connects to the JCR referred by the specified home directory and with the specified
     * configuration.
     * @param jcrHomePath the path of the home directory of the JCR.
     * @param conf the configuration from which a connection can be established.
     * @return a {@link OakRepository} instance representing the connected JCR repository.
     */
    OakRepository connect(final String jcrHomePath, final OakRepositoryConfiguration conf) {
      nodeStore = factory.create(jcrHomePath, conf);
      if (nodeStore != null) {
        Repository jcr = new Jcr(new Oak(nodeStore))
            .with(new SilverpeasSecurityProvider())
            .with("silverpeas")
            .createRepository();
        return new OakRepository(this, jcr);
      }
      return null;
    }

    /**
     * Disconnects the repository related by this connection.
     */
    void disconnect() {
      factory.dispose(nodeStore);
    }
  }
}
