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

package org.silverpeas.core.jcr.impl.oak.factories;

import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.jcr.impl.oak.configuration.OakRepositoryConfiguration;

/**
 * Factory of a CompositeNodeStore instance. This is to combine both document and segment storage.
 * See the
 * <a href="https://jackrabbit.apache.org/oak/docs/nodestore/compositens.html">documentation</a>
 * for more explanation about this storage. For instance, it isn't not supported by Silverpeas.
 * @author mmoquillon
 */
public class CompositeNodeStoreFactory implements NodeStoreFactory {

  @Override
  public NodeStore create(final String jcrHomePath, final OakRepositoryConfiguration conf) {
    throw new NotSupportedException("The composite node storage isn't yet supported!");
  }

}
