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
import org.silverpeas.core.jcr.SilverpeasRepository;
import org.silverpeas.core.jcr.impl.oak.factories.NodeStoreFactory;
import org.silverpeas.core.jcr.impl.oak.security.SilverpeasSecurityProvider;

import javax.jcr.Repository;

/**
 * <p>
 * Represents a repository in Oak. In the JCR API, a repository encapsulates the way the JCR is
 * accessed and in Oak it wraps the node storage in which is actually stored the content of the
 * JCR. There are different kinds of storage. Each of them taken in charge by a dedicated
 * {@link NodeStoreFactory} class. Hence, in Oak, the repository is created by specifying which
 * node store to use as datasource. The repository provides a method to shutdown the connection
 * with the underlying JCR once latter isn't anymore required; the method frees any eventual
 * resources allocated by Oak.
 * @author mmoquillon
 */
public class OakRepository extends SilverpeasRepository {

  /**
   * Creates a repository with as backend the specified node store.
   * @param store the node store to use to store all the content of the JCR tree.
   * @return a repository whose implementation is provided by Apache Jackrabbit Oak.
   */
  static OakRepository create(final NodeStore store) {
    if (store != null) {
      Repository jcr = new Jcr(new Oak(store)).with(new SilverpeasSecurityProvider())
          .with("silverpeas")
          .createRepository();
      return new OakRepository(jcr);
    }
    return null;
  }

  private OakRepository(final Repository repository) {
    super(repository);
  }

  void shutdown() {
    Repository repository = getRepository();
    if (repository instanceof JackrabbitRepository) {
      ((JackrabbitRepository) repository).shutdown();
    }
  }
}
