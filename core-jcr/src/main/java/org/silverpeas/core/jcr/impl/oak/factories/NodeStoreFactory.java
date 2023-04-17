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

package org.silverpeas.core.jcr.impl.oak.factories;

import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;

/**
 * A factory of a {@link NodeStore} objects. The concrete type of the node store is defined by the
 * repository configuration brought by a {@link OakRepositoryConfiguration} instance. A
 * {@link NodeStore} instance represents the storage the JCR repository will use as backend to store
 * and retrieve data in the form of nodes.
 */
public interface NodeStoreFactory {

  /**
   * Creates a {@link NodeStore} instance according to the specified configuration parameters for
   * the repository located at the given absolute path. The storage is opened and handled by the
   * specified {@link NodeStore} instance.
   * @param jcrHomePath the absolute path of the home directory of the JCR.
   * @param conf the JCR configuration with the parameters required to either create and initialize
   * or to open the node storage.
   * @return a {@link NodeStore} instance or null if the parameters in the configuration doesn't
   * match the type of node this factory is in charge of. The returned {@link NodeStore} instance is
   * the object by which the access to the storage is performed.
   */
  NodeStore create(final String jcrHomePath, final OakRepositoryConfiguration conf);

}
